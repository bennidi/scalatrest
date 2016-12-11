package com.github.bennidi.scalatrest.base

import com.github.bennidi.scalatrest.api.core.auth.{JwtManager, JwtAuthFilter}
import com.github.bennidi.scalatrest.api.core.{ResourceInfoProvider, LogSupport}
import com.github.bennidi.scalatrest.core.json.{JsonFormatProvider, SchemaValidator, JsonUtils}
import com.github.bennidi.scalatrest.core.json.JsonUtils._
import com.github.bennidi.scalatrest.model.base.{ManifestProvider, UUIDentifiable}
import com.github.bennidi.scalatrest.persistence.RepositorySupport
import org.json4s.JsonAST._
import org.json4s.jackson.Serialization._
import org.json4s.{StreamInput, DefaultFormats, Formats, NoTypeHints}
import org.json4s.jackson.Serialization

import scala.util.Random

/**
 * Provides convenience methods to issue HTTP requests to mounted REST resources.
 * Each request takes a block of code to execute when the call has been made
 */
abstract class RestSpec[T <: UUIDentifiable]( implicit val manifest: Manifest[T] )
  extends IntegrationSpec with JsonFormatProvider {

  val jwtManager = inject[JwtManager]

  def testUserOkid():String = "bd113898-cfb5-430a-910f-7c527776dcd8"

  object Headers {
    val tokenExpiry = System.currentTimeMillis() + 99999
    val token = jwtManager.encode(s"""{"sub":"${testUserOkid()}","roles":"ROLE_USER","iat":${System.currentTimeMillis()},"exp":${tokenExpiry}}""")
    val authenticationHeader = Map(JwtAuthFilter.AuthHeader -> s"""Bearer ${token}""")
    val ContentTypeJson = Map( "Content-Type" -> "application/json" )
    val All = ContentTypeJson ++ authenticationHeader
  }

  override def delete[A](uri: String, params: Iterable[(String, String)] = Seq.empty, headers: Iterable[(String, String)] = Seq.empty)(f: => A): A =
    submit("DELETE", uri, params, Headers.All ++ headers) { f }

  override def get[A](uri: String)(f: => A): A = get(uri = uri, headers = Headers.All)(f)

  def toJsonString[A <: scala.AnyRef]( a: A ): String = {
    write( a )
  }

  def postJson[A]( uri: String, body: AnyRef, headers: Map[String, String] = Map( ) )( f: => A ): A = {
    val _body:String = body match {
      case x:String => x
      case _ => toJsonString(body)
    }
    post( uri, _body, Headers.All ++ headers )( f )
  }


  def putJson[A]( uri: String, body: AnyRef, headers: Map[String, String] = Map( ) )( f: => A ): A ={
    val _body:String = body match {
      case x:String => x
      case _ => toJsonString(body)
    }
    put( uri, toJsonString( body ), Headers.All ++ headers )( f )
  }


  def patchJson[A]( uri: String, body: AnyRef, headers: Map[String, String] = Map( ) )( f: => A ): A ={
    val _body:String = body match {
      case x:String => x
      case _ => toJsonString(body)
    }
    patch( uri, toJsonString( body ), Headers.All ++ headers )( f )
  }


}

/**
 * Implement this trait if your suite needs a pre-initialized collection.
 *
 * @tparam T The type of persistent object that is handled by this fixture
 */
trait TestFixtureProvider[T <: UUIDentifiable] extends ManifestProvider[T] with JsonFormatProvider {

  import JsonUtils._

  protected lazy val fixture: TestFixture = readFixture( )

  def readFixture( ) = {
    load( getSimpleClassName( ) + ".fixture.json" ).extract[TestFixture];
  }

  def full( )( implicit manifest: Manifest[T] ): T = {
    fixture.full.extract[T]
  }

  // This method is broken because of unknown issues with json extractor
  def seed( )( implicit manifest: Manifest[T] ): Array[T] = {
    fixture.seed.extract[Array[T]]
  }

  def patches( ): List[PatchSpecification] = {
    fixture.patches
  }

  // This method will be called whenever a random non-existing item is needed
  def newRandom( ): T = {
    randomJObject( ).extract[T]
  }

  def randomJObject( ): JObject = {
    val randomized = fixture.random transformField {
      case (name, JString( "%random.string%" )) => (name, JString( UUIDentifiable.generateId( ) ))
      case (name, JString( "%random.uuid%" ))   => (name, JString( UUIDentifiable.generateId( ) ))
      case (name, JString( "%random.int%" ))    => (name, JInt( Random.nextInt( ) ))
    }
    randomized.asInstanceOf[JObject]
  }

  def existingItems: Array[T] = seed( )

  def itemWithId( id: String ): String = "\"_id\":\"" + id + "\""

}

case class TestFixture( seed: JArray, patches: List[PatchSpecification], full: JObject, random: JObject )

case class PatchSpecification( orig: JObject, patch: JObject, result: JObject )

/**
 * The layout defines properties that are needed by GenericCRUDSpec
 *
 * @tparam T
 */
trait CRUDSpecBase[T <: UUIDentifiable]
  extends RestSpec[T]
          with ManifestProvider[T] with LogSupport
          with ResourceInfoProvider with TestFixtureProvider[T]
          with TestFixtureExecutor[T] with RepositorySupport[T]{



}

/**
 * A generic specification of the semantics of CRUD operations for any type of resource
 * CRUD tests only need an endpoint and some information on the structure of the respective
 * resource.
 *
 * @tparam T The type of resource handled by this CRUD test
 */
trait GenericCRUDSpec[T <: UUIDentifiable] extends CRUDSpecBase[T]
                                                   with ReadSingleItemSpec[T]
                                                   with ReadMultipleItemsSpec[T]
                                                   with CreateMultipleItemsSpec[T]
                                                   with PatchSingleItemsSpec[T]
                                                   with DeleteSingleItemsSpec[T]
                                                   with VerifyJsonSchema[T]

trait ReadSingleItemSpec[T <: UUIDentifiable] extends CRUDSpecBase[T] {

  feature( s"A client can read single items from a $domainObject REST resource" ) {

    info( "As a client" )
    info( s"I want to be able to access an existing $domainObject" )

    scenario( "I issue a GET request without parameters to non-existing id" ) {

      Given( s"An endpoint mounted as '$endPointBaseUrl/:id'" )

      When( "the GET is sent" )
      Then( "a 404 is returned indicating that the resource could not be found" )

      seed( )
      get( s"$endPointBaseUrl/non-existing-id" ) {
        status should equal( 404 )
      }
    }

    scenario( "I issue a GET request without parameters to existing id" ) {

      Given( s"An endpoint mounted as '$endPointBaseUrl/:id'" )
      val id = existingItems( 0 ).id
      And( s"an existing $domainObject with id $id" )
      When( "an the GET is sent " )
      Then( s"a 200 containing the $domainObject is returned " )

      get( s"$endPointBaseUrl/$id" ) {
        status should equal( 200 )
        body should include( itemWithId( id ) )
      }
    }
  }

}

trait VerifyJsonSchema[T <: UUIDentifiable] extends CRUDSpecBase[T] {

  feature( s"The jsonschema for $domainObject provided by the REST resource '$endPointBaseUrl/jsonschema' " +
    s" is in sync with the domain model" ) {

    info( "As a client" )
    info( s"I rely on the provided jsonschema definition for $domainObjects" )

    scenario( s"I do a parameter-less GET to retrieve the schema for $domainObjects" ) {

      Given( s"a JSON schema provided by the respective endpoint" )

      When( "the schema is used to validate a domain object based on the scala domain model" )
      Then( "the validation should succeed" )

      get( endPointBaseUrl + "/jsonschema" ) {
        status should equal( 200 )
        // retrieved schema should accept all valid items
        val itemToCheck = toJson( newRandom( ) )
        val validationResult = SchemaValidator.verify( itemToCheck, parseJsonString( body ) )
        if ( !validationResult.isEmpty ) {
          val errorMsg = SchemaValidator.prettyPrint( validationResult )
          log.error(
            "Schema validation results for " + endPointBaseUrl + "/jsonschema \n" +
              "Violating instance: " + render( itemToCheck ) + "\n" + errorMsg )
        }
        validationResult.isEmpty should be( true )

      }
    }

  }

}

trait ReadMultipleItemsSpec[T <: UUIDentifiable] extends CRUDSpecBase[T] {

  feature( s"A client can read from a $domainObject REST resource" ) {

    info( "As a client" )
    info( s"I want to be able to browse available $domainObjects" )

    scenario( s"I do a parameter-less GET to retrieve the full list of $domainObjects" ) {

      Given( s"An endpoint mounted as '$endPointBaseUrl'" )

      When( "an HTTP GET is sent" )
      Then( "a full list is returned" )

      get( endPointBaseUrl ) {
        status should equal( 200 )
        for ( existing <- existingItems ) body should include( itemWithId( existing.id ) )
      }
    }

  }

}

trait DeleteSingleItemsSpec[T <: UUIDentifiable] extends CRUDSpecBase[T] {
  self: RepositorySupport[T] =>

  import reactivemongo.extensions.Implicits._

  feature( s"A client can delete  $domainObject REST resource" ) {

    info( "As a client " )
    info( s"I want to be able to create $domainObjects" )

    scenario( "I delete an insurance company with an empty DELETE on its identifier" ) {

      Given( s"an endpoint mounted as '$endPointBaseUrl/:id'" )
      And( s"a randomly selected existing $domainObject" )
      val ics = newRandom( )
      val item = for {
        _ <- repository.insert( ics )
        inserted <- ~repository.findById( ics.id )
      } yield ( inserted )

      whenReady( item ) {
        case inserted =>

          When( "an HTTP DELETE is sent" )

          delete( s"$endPointBaseUrl/" + inserted.id ) {
            status should equal( 200 )
            body should include( itemWithId( inserted.id ) )
          }
          Then( s"the respective $domainObject is deleted" )
          get( s"$endPointBaseUrl/" + inserted.id ) {
            status should equal( 404 )
          }
      }
    }

  }
}

trait CreateMultipleItemsSpec[T <: UUIDentifiable] extends CRUDSpecBase[T] {
  self: RepositorySupport[T] =>

  feature( s"A client can create new $domainObjects using POST" ) {

    info( "As a client " )
    info( s"I want to be able to create $domainObjects" )

    scenario( s"I use POST with an array of $domainObjects" ) {

      Given( s"an endpoint mounted as '$endPointBaseUrl'" )
      And( s"a set of non-existing $domainObjects" )
      val items = Array( newRandom( ), newRandom( ) )
      When( s"an HTTP POST is send with an array of $domainObjects" )
      Then( s"the $domainObjects are persistently created and returned as a list" )

      postJson( endPointBaseUrl, body = items ) {
        status should equal( 201 )
        body should include( itemWithId( items( 0 ).id ) )
        body should include( itemWithId( items( 1 ).id ) )
      }
    }

  }

}

trait CreateSingleItemSpec[T <: UUIDentifiable] extends CRUDSpecBase[T] {
  self: RepositorySupport[T] =>

  feature( s"A client can create new $domainObjects using POST" ) {

    info( "As a client " )
    info( s"I want to be able to create a single $domainObject" )

    scenario( s"I use POST to add a single $domainObject" ) {

      Given( s"an endpoint mounted as '$endPointBaseUrl'" )
      And( s"a non-existing $domainObject" )
      val item = newRandom( )
      When( s"an HTTP POST is sent with an  $domainObject as payload" )
      Then( s"the $domainObject is persistently created and returned as a list" )

      postJson( endPointBaseUrl, body = item ) {
        status should equal( 201 )
        body should include( itemWithId( item.id ) )
      }
    }

  }

}

trait PatchSingleItemsSpec[T <: UUIDentifiable] extends CRUDSpecBase[T] {
  self: RepositorySupport[T] =>

  def ignoreMissingPatchesInFixture = false

  feature( s"A client can update an existing $domainObject using PATCH" ) {

    info( "As a client " )
    info( s"I want to be able to update existing $domainObjects" )

    scenario( s"I want to update an existing $domainObject using delta update" ) {
      if ( !ignoreMissingPatchesInFixture )
        patches( ).size should be > 0
      Given( s"An endpoint mounted as '$endPointBaseUrl/:id'" )
      And( s"some existing $domainObjects" )
      val originals = patches( ) map ( _.orig.extract[T] )
      val itemsToPatch = for {
        newItems <- repository.bulkInsert( originals )
        retrieved <- repository.findByIds( originals map ( _.id ): _* )
      } yield ( retrieved )


      When( "I send a partially updated view via PATCH to the resource" )
      Then( s"the respective $domainObject is updated with the new properties" )
      whenReady( itemsToPatch ) {
        case ( retrieved ) =>
          patches( ) foreach ( ( patchSpec ) => {
            val expectedResult = patchSpec.result.extract[T]
            patchJson( s"$endPointBaseUrl/" + expectedResult.id, body = patchSpec.patch ) {
              status should equal( 200 )
              val result = parseJson( body ).extract[T]
              result should equal( expectedResult )
            }
          } )

      }
    }

  }
}