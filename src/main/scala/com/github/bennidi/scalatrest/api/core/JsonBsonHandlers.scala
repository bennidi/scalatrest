package com.github.bennidi.scalatrest.api.core

import java.nio.ByteBuffer

import org.apache.commons.codec.binary.Hex
import org.jboss.netty.buffer.ChannelBuffers
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s._
import reactivemongo.bson._

import scala.util.{Failure, Success, Try}

/**
 * Implement this trait to have access to implicit converters. This is helpful because it is hard for an IDE
 * to detect usage of those imports and they might get "optimized" away.
 */
trait JObjectToBson {

  implicit val writer:BSONDocumentWriter[JObject] = Json4sToBson.writer
  implicit val reader:BSONDocumentReader[JObject] = Json4sToBson.reader

}


/**
 *
 * Defines BSON reader and writer that allow to use json4s [[JObject]] within domain model classes
 * They convert JObject<->BSONDocument
 *
 * Inspired by
 * ===========
 * http://stackoverflow.com/questions/31528622/reactivemongo-mixed-type-map-to-bsondocument
 * https://github.com/Jacoby6000/Json4s-reactivemongo-compatibility
 * https://github.com/ReactiveMongo/Play-ReactiveMongo/blob/master/src/main/scala/play/modules/reactivemongo/json.scala
 * https://gist.github.com/rleibman/7103325d0193be268ed7
 * https://gist.github.com/nevang/4690568
 */
object Json4sToBson extends LogSupport{

  implicit val writer = new BSONDocumentWriter[JObject] {

    override def write( json: JObject ): BSONDocument = {
      // JObject.values is a Map of its fields
      BSONDocument( json.values.map( writePair ) )
    }

    private def writeArray( arr: List[_] ): BSONArray = {
      val values = arr.map {transform(_)}
      BSONArray( values )
    }

    private val IsoDateTime = """^(\d{4,})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})\.(\d{3})Z$""".r

    private def manageDate( year: String, month: String, day: String, hour: String, minute: String, second: String, milli: String ) =
      Try( BSONDateTime( ( new DateTime( year.toInt, month.toInt, day.toInt, hour.toInt,
        minute.toInt, second.toInt, milli.toInt, DateTimeZone.UTC ) ).getMillis ) )

    private def manageTimestamp( o: JObject ) = o.values.toList match {
      case ("t", JInt( t )) :: ("i", JInt( i )) :: Nil =>
        Success( BSONTimestamp( ( t.toLong & 4294967295L ) | ( i.toLong << 32 ) ) )
      case _                                           => Failure(
        new IllegalArgumentException( "Illegal timestamp value" ) )
    }

    private def manageSpecials( obj: JObject ): BSONValue =
      if ( obj.values.size > 2 ) writeObject( obj )
      else ( obj.values.toList match {
        case ("$oid", JString( str )) :: Nil => Try(
          BSONObjectID( Hex.decodeHex( str.toArray ) ) )
        case ("$undefined", JNothing) :: Nil => Success( BSONUndefined )
        // Bug on reactivemongo
        case ("$minKey", JInt( n )) :: Nil if n == 1                       => Success( BSONMinKey )
        case ("$maxKey", JInt( n )) :: Nil if n == 1                       => Success( BSONMaxKey )
        case ("$js", JString( str )) :: Nil                                => Success( BSONJavaScript( str ) )
        case ("$sym", JString( str )) :: Nil                               => Success( BSONSymbol( str ) )
        case ("$jsws", JString( str )) :: Nil                              => Success( BSONJavaScriptWS( str ) )
        case ("$timestamp", ts: JObject) :: Nil                            => manageTimestamp( ts )
        case ("$regex", JString( r )) :: ("$options", JString( o )) :: Nil =>
          Success( BSONRegex( r, o ) )
        case ("$binary", JString( d )) :: ("$type", JString( t )) :: Nil   =>
          Try(
            BSONBinary(
              ChannelBuffers.wrappedBuffer( Hex.decodeHex( d.toArray ) ).array( ),
              findSubtype( Hex.decodeHex( t.toArray ) ) ) )
        // case ("$ref", JString(v)) :: ("$id", JString(i)) :: Nil => // Not implemented
        //   Try(BSONDBPointer(v, Hex.decodeHex(i.toArray)))
        case _ => Success( writeObject( obj ) )
      } ) match {
        case Success( v ) => v
        case Failure( _ ) => writeObject( obj )
      }

    def findSubtype( bytes: Array[Byte] ) =
      ByteBuffer.wrap( bytes ).getInt match {
        case 0x00 => Subtype.GenericBinarySubtype
        case 0x01 => Subtype.FunctionSubtype
        case 0x02 => Subtype.OldBinarySubtype
        case 0x03 => Subtype.UuidSubtype
        case 0x05 => Subtype.Md5Subtype
        // case 0X80 => Subtype.UserDefinedSubtype // Bug on reactivemongo
        case _ => throw new IllegalArgumentException( "unsupported binary subtype" )
      }

    private def writeObject( obj: JObject ): BSONDocument = BSONDocument( obj.values.map( writePair ) )

    private def writePair( p: (String, Any) ): (String, BSONValue) = {
      (p._1, transform(p._2))
    }

    private def transform( value:Any ): BSONValue = {
      value match {
        case JString( str@IsoDateTime( y, m, d, h, mi, s, ms ) ) => manageDate( y, m, d, h, mi, s, ms ) match {
          case Success( dt ) => dt
          case Failure( _ )  => BSONString( str )
        }
        case JString( str )                                      => BSONString( str )
        case JDouble( value )                                    => BSONDouble( value.doubleValue )
        case JBool( value )                                      => BSONBoolean( value )
        case JNull                                               => BSONNull
        case arr: JArray                                         => writeArray( arr.children )
        case list: List[_]                                       => writeArray( list )
        case obj: JObject                                        => manageSpecials( obj )
        case str: String                                         => BSONString( str )
        case value: Double                                       => BSONDouble( value )
        case number: BigInt                                      => BSONInteger( number.intValue( ) )
        case map: Map[_, _]                                      => BSONDocument( map.asInstanceOf[Map[String,_]].map( writePair ) )
        case catchAll:Any                                        => {
          log.error("Could not transform " + catchAll + " into its BSON form")
          BSONString( "BSON transformation error" )
        }
      }
    }
  }

  implicit val reader = new BSONDocumentReader[JObject] {
    override def read(bson: BSONDocument): JObject = {
      JObject(bson.elements.toSeq.map(readElement):_*)
    }
    private def readObject(doc: BSONDocument) = {
      val fields = doc.elements.toSeq.map(readElement)
      JObject(fields:_*)
    }
    private val isoFormatter = ISODateTimeFormat.dateTime.withZone(DateTimeZone.UTC)

    private def readElement(e: BSONElement): JField = e._1 -> (e._2 match {
      case BSONString(value)         => JString(value)
      case BSONInteger(value)        => JInt(value)
      case BSONLong(value)           => JInt(value)
      case BSONDouble(value)         => JDouble(value)
      case BSONBoolean(true)         => JBool(true)
      case BSONBoolean(false)        => JBool(false)
      case BSONNull                  => JNull
      case doc: BSONDocument         => readObject(doc)
      case arr: BSONArray            => readArray(arr)
      case oid @ BSONObjectID(value) => JObject("$oid" -> JString(oid.stringify))
      case BSONDateTime(value)       => JString(isoFormatter.print(value)) // NOTE: Doesn't follow mongdb-extended
      case bb: BSONBinary            => readBSONBinary(bb)
      case BSONRegex(value, flags)   => JObject("$regex" -> JString(value), "$options" -> JString(flags))
      case BSONTimestamp(value) => JObject("$timestamp" -> JObject(
        "t" -> JInt(value.toInt), "i" -> JInt((value >>> 32).toInt)))
      case BSONUndefined           => JObject("$undefined" -> JBool(true))
      case BSONMinKey                   => JObject("$minKey" -> JInt(1)) // NOTE: Bug on reactivemongo
      //case BSONMaxKey              => JObject("$maxKey" -> JNumber(1))
      // case BSONDBPointer(value, id) => JObject("$ref" -> JString(value), "$id" -> JString(Hex.encodeHexString(id))) // Not implemented
      // NOT STANDARD AT ALL WITH JSON and MONGO
      case BSONJavaScript(value)   => JObject("$js" -> JString(value))
      case BSONSymbol(value)       => JObject("$sym" -> JString(value))
      case BSONJavaScriptWS(value) => JObject("$jsws" -> JString(value))
    })

    private def readArray(array: BSONArray) = JArray(array.iterator.toList.map(element => readElement(element.get)._2))

    private def readBSONBinary(bb: BSONBinary) = {
      val arr = new Array[Byte](bb.value.readable())
      bb.value.readBytes(arr)
      val sub = ByteBuffer.allocate(4).putInt(bb.subtype.value).array
      JObject("$binary" -> JString(Hex.encodeHexString(arr)),
        "$type" -> JString(Hex.encodeHexString(sub)))
    }
  }

}

// Inspired by : http://stackoverflow.com/questions/31528622/reactivemongo-mixed-type-map-to-bsondocument
/*
object AnyMapToBson {

  val writer = new BSONDocumentWriter[Map[String, _ <: Any]] {

    override def write( map: Map[String, _ <: Any] ): BSONDocument = {
      BSONDocument( map.map( writePair ) )
    }

    private def writeSeq( seqence: Seq[Any] ): BSONArray = {
      //val transformed = arr.map ( x => writeValue(x))
      BSONArray( seqence.map { case x => writeValue(x) } )
    }

    private val IsoDateTime = """^(\d{4,})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})\.(\d{3})Z$""".r

    private def manageDate( year: String, month: String, day: String, hour: String, minute: String, second: String, milli: String ) =
      Try( BSONDateTime( ( new DateTime( year.toInt, month.toInt, day.toInt, hour.toInt,
        minute.toInt, second.toInt, milli.toInt, DateTimeZone.UTC ) ).getMillis ) )

    private def manageTimestamp( o: Map[String, Any] ) = o.values.toList match {
      case ("t", JInt( t )) :: ("i", JInt( i )) :: Nil =>
        Success( BSONTimestamp( ( t.toLong & 4294967295L ) | ( i.toLong << 32 ) ) )
      case _                                           => Failure(
        new IllegalArgumentException( "Illegal timestamp value" ) )
    }

    private def manageSpecials( obj: Map[String, Any] ): BSONValue =
      if ( obj.values.size > 2 ) writeMap( obj )
      else ( obj.values.toList match {
        case ("$undefined", Nil) :: Nil => Success( BSONUndefined )
        // Bug on reactivemongo
        case ("$minKey", n: Integer) :: Nil if n == 1    => Success( BSONMinKey )
        case ("$maxKey", JInt( n )) :: Nil if n == 1     => Success( BSONMaxKey )
        case ("$js", str: String) :: Nil                 => Success( BSONJavaScript( str ) )
        case ("$sym", str: String) :: Nil                => Success( BSONSymbol( str ) )
        case ("$jsws", str: String) :: Nil               => Success( BSONJavaScriptWS( str ) )
        case ("$timestamp", ts: Map[String, Any]) :: Nil => manageTimestamp( ts )

        case _ => Success( writeMap( obj ) )
      } ) match {
        case Success( v ) => v
        case Failure( _ ) => writeMap( obj )
      }

    def findSubtype( bytes: Array[Byte] ) =
      ByteBuffer.wrap( bytes ).getInt match {
        case 0x00 => Subtype.GenericBinarySubtype
        case 0x01 => Subtype.FunctionSubtype
        case 0x02 => Subtype.OldBinarySubtype
        case 0x03 => Subtype.UuidSubtype
        case 0x05 => Subtype.Md5Subtype
        // case 0X80 => Subtype.UserDefinedSubtype // Bug on reactivemongo
        case _ => throw new IllegalArgumentException( "unsupported binary subtype" )
      }

    private def writeMap( obj: Map[String, Any] ): BSONDocument = BSONDocument( obj.map( writePair ) )

    private def writePair( p: (String, Any) ): (String, BSONValue) = {
      (p._1, writeValue( p._2 ))
    }

    private def writeValue( v: Any ): BSONValue = {
      v match {
        case str@IsoDateTime( y, m, d, h, mi, s, ms ) => manageDate( y, m, d, h, mi, s, ms ) match {
          case Success( dt ) => dt
          case Failure( _ )  => BSONString( "failed" )
        }
        // Handle scalar data types
        // Note: This list is not yet exhaustive and might need stepwise extension
        case str: String         => BSONString( str )
        case bool: Boolean       => BSONBoolean( bool )
        case int: Integer        => BSONInteger( int )
        case int: Int            => BSONInteger( int )
        case double: Double      => BSONDouble( double )
        case value: Long         => BSONLong( value )
        case number: ScalaNumber => BSONInteger( number.intValue( ) )

        case seq:Seq[Any]       => writeSeq(seq)
        case obj: Map[String,Any] => manageSpecials( obj )
        case Nil                   => BSONNull
        case address:Address       => BsonConversions.conversionFor(address).write(address)
        case company:InsuranceCompany       => BsonConversions.conversionFor(company).write(company)
        case _                     => BSONString( v.toString + "@" + v.getClass.toString )
      }
    }

  }

  val reader = new BSONDocumentReader[Map[String, _<:Any]] {
    override def read( bson: BSONDocument ): Map[String, _<:Any] = {
      Map[String, Any]( bson.elements.toSeq.map( readElement ): _* )
    }

    private def readObject( doc: BSONDocument ) = {
      Map[String, Any]( doc.elements.toSeq.map( readElement ): _* )
    }

    private val isoFormatter = ISODateTimeFormat.dateTime.withZone( DateTimeZone.UTC )

    private def readElement( e: BSONElement ): (String, Any) = e._1 -> ( e._2 match {
      case BSONString( value )  => value
      case BSONInteger( value ) => value
      case BSONLong( value )    => value
      case BSONDouble( value )  => value
      //case BSONBoolean(true)         => JTrue
      //case BSONBoolean(false)        => JFalse
      case BSONNull                  => Nil
      case doc: BSONDocument         => readObject( doc )
      case arr: BSONArray            => readArray( arr )
      case oid@BSONObjectID( value ) => Map[String, Any]( "$oid" -> oid.stringify )
      case BSONDateTime( value )     => isoFormatter.print( value ) // Doesn't follow mongdb-extended
      case bb: BSONBinary            => readBSONBinary( bb )
      //case BSONRegex(value, flags)   => Map[String, Any]("$regex" -> String(value), "$options" -> String(flags))
      case BSONTimestamp( value ) => Map[String, Any]( "$timestamp" -> Map[String, Any](
        "t" -> value.toInt, "i" -> ( value >>> 32 ).toInt ) )
      case BSONUndefined          => Map[String, Any]( "$undefined" -> true )
      // case BSONMinKey                   => Map[String, Any]("$minKey" -> JNumber(1)) // Bug on reactivemongo
      case BSONMaxKey => Map[String, Any]( "$maxKey" -> 1 )
      // case BSONDBPointer(value, id) => Map[String, Any]("$ref" -> String(value), "$id" -> String(Hex.encodeHexString(id))) // Not implemented
      // NOT STANDARD AT ALL WITH JSON and MONGO
      case BSONJavaScript( value )   => Map[String, Any]( "$js" -> value )
      case BSONSymbol( value )       => Map[String, Any]( "$sym" -> value )
      case BSONJavaScriptWS( value ) => Map[String, Any]( "$jsws" -> value )
    } )

    private def readArray( array: BSONArray ) = Array(
      array.iterator.toSeq.map( element => readElement( element.get )._2 ) )

    private def readBSONBinary( bb: BSONBinary ) = {
      val arr = new Array[Byte]( bb.value.readable( ) )
      bb.value.readBytes( arr )
      val sub = ByteBuffer.allocate( 4 ).putInt( bb.subtype.value ).array
      Map[String, Any]( "$binary" -> Hex.encodeHexString( arr ),
        "$type" -> Hex.encodeHexString( sub ) )
    }
  }

}*/

/**
 *
 * Defines implicit readers and writers for single typed maps.
 *
 * Taken from: http://codeengine.org/reactivemongo-mixed-type-map-to-bsondocument/
 */
object MapBSON {

  implicit def MapReader[V <: Any](implicit vr: BSONDocumentReader[V]): BSONDocumentReader[Map[String, V]] = new BSONDocumentReader[Map[String, V]] {
    def read(bson: BSONDocument): Map[String, V] =
      bson.elements.map { tuple =>
        // assume that all values in the document are BSONDocuments
        tuple._1 -> vr.read(tuple._2.seeAsTry[BSONDocument].get)
      }.toMap
  }

  implicit def MapWriter[V <: Any](implicit vw: BSONDocumentWriter[V]): BSONDocumentWriter[Map[String, V]] = new BSONDocumentWriter[Map[String, V]] {
    def write(map: Map[String, V]): BSONDocument = BSONDocument {
      map.toStream.map { tuple =>
        tuple._1 -> vw.write(tuple._2)
      }
    }
  }
}

object MapValue {
  implicit def MapReader[V <: Any](implicit vr: BSONReader[_ <: BSONValue, V]): BSONDocumentReader[Map[String, V]] = new BSONDocumentReader[Map[String, V]] {
    def read(bson: BSONDocument): Map[String, V] =
      bson.elements.map { tuple =>
        tuple._1 -> vr.asInstanceOf[BSONReader[BSONValue, V]].read(tuple._2)
      }.toMap
  }

  implicit def MapWriter[V <: Any](implicit vw: BSONWriter[V, _ <: BSONValue]): BSONDocumentWriter[Map[String, V]] = new BSONDocumentWriter[Map[String, V]] {
    def write(map: Map[String, V]): BSONDocument = BSONDocument {
      map.toStream.map { tuple =>
        tuple._1 -> vw.write(tuple._2)
      }
    }
  }

  implicit def MapHandler[V](implicit vr: BSONReader[_ <: BSONValue, V], vw: BSONWriter[V, _ <: BSONValue]): BSONHandler[BSONDocument, Map[String, V]] = new BSONHandler[BSONDocument, Map[String, V]] {
    private val reader = MapReader[V]
    private val writer = MapWriter[V]
    def read(bson: BSONDocument): Map[String, V] = reader read bson
    def write(map: Map[String, V]): BSONDocument = writer write map
  }
}