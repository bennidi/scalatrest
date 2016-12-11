package com.github.bennidi.scalatrest.core.json

import com.fasterxml.jackson.core.JsonParseException
import com.github.bennidi.scalatrest.api.core.LogSupport
import com.github.bennidi.scalatrest.api.error.InvalidJsonError
import org.json4s.JsonAST.{JObject, JArray, JNothing, JValue}
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import org.json4s.prefs.EmptyValueStrategy
import org.json4s.reflect.Reflector

import scala.concurrent.Future

/**
 * Convenience abstraction over underlying Json4s implementations.
 * Exposes only higher level methods for manipulating Json without to many details
 *
 */
object JsonUtils extends LogSupport {

  object JsonMethodsSupport extends org.json4s.jackson.JsonMethods

  implicit def jsonFormats: Formats = new DefaultFormats  {
    /**
     * Smart Type Hints are currently unused but could
     * be used to support handling of polymorphic lists
     *
     * {{{
     *   SmartTypeHints( typeMappings = Map(
     *  "EmailHandle" -> "com.github.bennidi.scalatrest.model.interactions.EmailHandle",
     *  "PhoneHandle" -> "com.github.bennidi.scalatrest.model.interactions.PhoneHandle",
     *  "LegalEntity" -> "com.github.bennidi.scalatrest.model.interactions.LegalEntity",
     *  "KeyAccountManager" -> "com.github.bennidi.scalatrest.model.interactions.KeyAccountManager"
    *   ) )
     * }}}
     */
    override val typeHints = SmartTypeHints( typeMappings = Map())
    override val strictOptionParsing: Boolean = true
    override val emptyValueStrategy: EmptyValueStrategy = EmptyValueStrategy.skip
    override val allowNull: Boolean = true
  }


  def parseJson( in: org.json4s.JsonInput, useBigDecimalForDouble: scala.Boolean = false ) = {
    try {
      JsonMethodsSupport.parse( in, useBigDecimalForDouble )
    } catch {
      case e:JsonParseException => throw InvalidJsonError(e.getMessage)
      case other:Throwable => throw other
    }
  }

  def load(resource:String):JValue = {
    parseJson( getClass.getClassLoader.getResourceAsStream( resource ) )
  }

  def format( value: org.json4s.JValue ): JValue = {
    JsonMethodsSupport.render( value )
  }

  def toJson[A <: scala.AnyRef]( a: A ): JValue = {
    parseJson( write( a ) )
  }

  /**
   * Wraps a given JSON structure in an array (if not already an array)
   */
  def wrap( json: JValue ): JValue = {
    json match {
      // Array is expected and simply re-exposed as attribute
      case JArray( _ ) | JNothing => json
      // Any other value is wrapped in an array and re
      case _ => JArray( List( json ) )
    }
  }

  def render( json: JValue ): String = {
    JsonMethodsSupport.pretty( json )
  }

  def parseJsonString( json: String ): JValue = {
    parseJson( new StringInput( json ) )
  }

  /**
   * Remove all collections (array) from a given Json structure
   */
  def removeCollections( main: JValue ): JValue = {
    main mapField {
      case (name: String, JArray( _ ))  => (name, JNothing)
      case (name: String, obj: JObject) => (name, removeCollections( obj ))
      case other                        => other
    }
  }

  /**
   * Merge two JSON objects using "overlay" semantics. The resulting object will contain exactly
   * the fields (key,value) of the specified update (overwriting the existing fields of the original)
   * AND all other fields as previously contained in the original.
   *
   * @param orig The object that is the base for the overlay
   * @param update The object that will be layered on top of it
   * @return The merged object according to the overlay semantics
   */
  def overlay( orig: JObject, update: JObject ): JValue = {
    // Internal overlay function using scala Map() which offers a more consistent functional interface
    // compared to json4s
    def overlayMaps( orig: Map[String, Any], update: Map[String, Any] ): Map[String, Any] = {
      val processed = scala.collection.mutable.HashMap.empty[String, Boolean]
      val partialMerge = orig map {
        // The new object is constructed by copying all fields from the original object
        // unless the update specifies a new value for it.
        // If a corresponding field is found in update then this field is used.
        case (name, value) if update.contains( name ) =>
          processed += ( name -> true )
          update.get( name ) match {
            // If the value is a nested object (another JObject), the same overlay procedure is applied
            case Some( newVal: Map[_, _] ) if value.isInstanceOf[Map[_,_]] =>
              // Usage of #instanceOf() is ugly but pattern matching on generic types is non-trivial and cast is actually type-safe
              // See: http://stackoverflow.com/questions/1094173/how-do-i-get-around-type-erasure-on-scala-or-why-cant-i-get-the-type-paramete
              (name, overlayMaps( value.asInstanceOf[Map[String, Any]], newVal.asInstanceOf[Map[String, Any]] ))
            case Some( newVal: Map[_, _] ) => (name, newVal)
            // Arrays and other values just override the original
            case Some( newVal: List[_] ) => (name, newVal)
            case Some( newVal: Any )     => (name, newVal)
            case None                    => (name, value)
          }
        // This case handles all fields that are not present in the update (simple copy)
        case (name, value) => (name, value)
      }
      val unProcessedFields = update collect { case (n, v) if !processed.contains( n ) => (n, v) }
      partialMerge ++ unProcessedFields
    }
    // Execute overlaying procedure in in Map() space
    toJson( overlayMaps( orig.values, update.values ) )
  }


  /**
   * Take a domain object of type T and apply a [partial] update to it.
   * The update is specified in JSON and the returned object is a newly constructed
   * object of type T reflecting the previous state with the additional update layered on top.
   * In contrast to the semantics of json4s merge() specified collections are replaced and not
   * merged by their elements.
   *
   * @param domainObject
   * @param update
   * @param m
   * @tparam T
   * @return
   */
  def merge[T <: AnyRef]( domainObject: T, update: JValue )( implicit m: Manifest[T] ): T = {
    val merged = overlay( toJson( domainObject ).asInstanceOf[JObject], update.asInstanceOf[JObject] )
    val mergedCompany = merged.extract[T]
    mergedCompany
  }

}

/**
 * Wherever an instance of [[Formats]] is required, this trait can be used
 */
trait JsonFormatProvider {
  // Implement the implicit required by JacksonJsonSupport
  implicit val jsonFormats: Formats = JsonUtils.jsonFormats
  implicit val formats = Serialization.formats( NoTypeHints )
}

/**
 * Resolve type hints dynamically. This allows for decoupling of dependencies using String representations
 * of classes instead of actual class references.
 *
 * TODO: Instead of providing explicit references to types they could be discovered based on annotations or similar
 *
 * @param typeMappings Tuples mapping a short class name to its fully qualified class name
 */
case class SmartTypeHints( typeMappings: Map[String, String] ) extends TypeHints with LogSupport {

  def resolveHints( hints: Iterable[String] ): List[Class[_]] = {
    val classes: List[Option[Class[_]]] = hints.collect { case hint: String => {
      try {Some( Class.forName( hint ) )} catch {case _: Throwable => None}
    }
    }.toList
    classes flatten
  }

  val hints = List( resolveHints( typeMappings.values ): _* )

  // resolve String base hints to
  def hintFor( clazz: Class[_] ) = clazz.getName.substring( clazz.getName.lastIndexOf( "." ) + 1 )

  //Short class name
  def classFor( hint: String ) = {
    try {
      Reflector.scalaTypeOf( typeMappings( hint ) ).map( _.erasure )
    } catch {
      case _: Throwable => {
        log.warn( s"Class for hint ${hint} not found" )
        None
      }
    }
  }
}
