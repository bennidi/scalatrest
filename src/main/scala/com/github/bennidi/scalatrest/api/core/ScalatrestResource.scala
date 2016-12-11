package com.github.bennidi.scalatrest.api.core

import com.github.bennidi.scalatrest.api.core.crud.RepositoryCrudOperations
import com.github.bennidi.scalatrest.api.core.json.{ScalatrestJsonSupport, POSTPUTSupport}
import com.github.bennidi.scalatrest.api.error.ApiException
import com.github.bennidi.scalatrest.api.model.{AccountRepository}
import com.github.bennidi.scalatrest.core.di.{DIModules}
import com.github.bennidi.scalatrest.core.json.JsonUtils
import com.github.bennidi.scalatrest.core.json.JsonUtils._
import com.github.bennidi.scalatrest.model.base.{ManifestProvider, UUIDentifiable}
import com.github.bennidi.scalatrest.persistence.RepositorySupport
import com.github.bennidi.scalatrest.persistence.core.GlobalExecutionContext
import org.json4s.JsonAST.{JObject, JValue}
import org.json4s.{JField, JString, _}
import org.scalatra._

/**
 * Composition of functionality (traits, implicits and other Scala magic) necessary
 * for convenient building of JSON REST resources
 *
 * Provides
 * ========
 *  -  Support for JSON handling
 *     -> read,write,format etc. using Jackson
 *     -> mapping between JSON and respective case classes
 *  - Integration with Swagger such that each resource is automatically discovered by the
 *  Swagger API documentation provider
 *  - REACTIVE: Transparent handling of Futures. REST operations may return Futures to
 * be automatically resolved within Scalatras [[RenderPipeline]]
 *
 */
abstract class ScalatrestResource[T <: UUIDentifiable] extends AbstractServletBase with ManifestProvider[T]
                                                           with ScalatrestJsonSupport with ApiDocumentationSupport with ResourceInfoProvider
                                                           with RepositorySupport[T] with DIModules
                                                           with ResourceStatusProvider
                                                           with POSTPUTSupport[T] with FutureSupport with HttpCallSupport {

  def errorMessage( msg: String ) = JObject(JField( "message", JString( msg ) ))
  def errorMessage( ex: Throwable ) = s"Exception in request (" + request("rid") + ")=>" + ex.getClass + " saying: "  + ex.getMessage

  /**
   * Top level error handlers. All exceptions that are not handled further down the callstack bubble
   * up to this point and will be transformed into appropriate responses to the client
   */
  error {
    // Meaningfully detectable errors are transformed into ApiException which contain error messages that
    // can be exposed to clients
    case ex:ApiException => {
      logError(errorMessage(ex), ex)
      ex.toHttpResponse()
    }
    // This is thrown by reactivemongo when an query by id returns nothing
    case ex:NoSuchElementException => {
      logError(errorMessage(ex), ex)
      NotFound( reason = "Non-existing resource",
        body = errorMessage( "This resource does not exist" ) ,
        headers = Map( "X-Error-Type" -> "Non-existing resource" ) )
    }
    // Dealing with any other throwable
    case ex => {
      logError(errorMessage(ex), ex)
      InternalServerError( reason = "Internal Server Error",
        body = errorMessage( "Internal Server Error" ),
        headers = Map( "X-Error-Type" -> "Internal Server Error" ) )
    }

  }

  /**
   * Called for any request to a route that does not exist
   */
  notFound {
    NotFound( body = errorMessage( "The resource you requested was not found on this server" ) )
  }

}

/**
 * Aggregates information about a Rest resource into a single object
 *
 * @param urlBase The base url for the endpoint
 * @param singular The name of the corresponding domain model (singular).
 *                 NOTE: This is used for lookup of associated json schema and fixtures
 * @param plural The name of the corresponding domain model (plural)
 * @param description A description of that resource
 */
case class RestResourceInfo(urlBase:String, singular:String, plural:String, description:String)
trait ResourceInfoProvider{
  def resourceInfo:RestResourceInfo
  def domainObject:String = resourceInfo.singular
  def domainObjects:String = resourceInfo.plural
  def endPointBaseUrl: String = resourceInfo.urlBase
}

/**
 * Load json schema file from classpath
 */
trait JsonSchemaReader{
  def readSchema( schemaDef:String):JObject = {
    val schema = schemaDef + ".schema.json"
    JsonUtils.load(schema).asInstanceOf[JObject];
  }
}
object JsonSchemaReader extends JsonSchemaReader


/**
 * Expose domain models JSON schema as an endpoint of the corresponding REST resource
 */
trait JsonSchemaProvider[T <: UUIDentifiable] extends JsonSchemaReader with ManifestProvider[T]{
  self: AbstractServletBase with ApiDocumentationSupport with ScalatrestJsonSupport =>

  get( "/jsonschema",
    apidoc( apiOperation[JValue]( "getJSONSchema" )
      summary "Retrieve the schema document that describes the resource type using JSON schema (draft 4)" ) ) {
    log.debug("Loading schema for " + getSimpleClassName())
    readSchema (getSimpleClassName()) // TODO: caching...
  }

}

/**
 * Attaches a "/status" route to each REST resource.
 * Returns 200 (OK) if resource is available and 500 (Internal Server Error) otherwise
 */
trait ResourceStatusProvider {
  self: AbstractServletBase with ApiDocumentationSupport with ScalatrestJsonSupport with GlobalExecutionContext =>

  private val statusRepository = inject[AccountRepository]

  get( "/status",
    apidoc( apiOperation[JValue]( "getStatus" )
      summary "Retrieve information about the status of this resource" ) ) {
    statusRepository.findRandom( )
  }
}

/**
 * Exposes basic CRUD operations as routes of the implementing REST resource.
 *
 * @tparam T
 */
abstract class CrudResource[T <: UUIDentifiable] extends ScalatrestResource[T] with RepositoryCrudOperations[T] with GetAll[T]
                                                       with GetById[T] with BatchCreateNew[T]
                                                       with DeleteById[T] with ReplaceById[T] with PatchById[T] {
}

trait GetAll[T <: UUIDentifiable] {
  self: ScalatrestResource[T] with RepositoryCrudOperations[T] =>

  get( "/", apidoc(
    apiOperation[List[T]]( s"list${domainObjects}" )
      summary s"Get a list of ${domainObjects}"
      notes s"List all available ${domainObjects}. You can filter by name."
      parameter queryParam[Option[String]]( "name" ).description( "Filter by name" ) ) ) {

    getAll( )
  }

}


trait DeleteById[T <: UUIDentifiable]{
  self : ScalatrestResource[T] with RepositoryCrudOperations[T] =>

  delete( "/:id",
    apidoc( apiOperation[T]( s"delete${domainObject}" )
      summary s"Deletes an existing ${domainObject}"
      notes s"In some case the object is not deleted but anonymized"
      parameter pathParam[String]( "id" ).description( s"The id of the ${domainObject} to be updated" ) ) ) {

    deleteExisting( params( "id" ) ) map {
      case domainObject: T => Ok( domainObject )
      case _               => NotFound( )
    }
  }
}

trait GetById[T <: UUIDentifiable] {
  self: ScalatrestResource[T] with RepositoryCrudOperations[T] =>

  get( "/:id",
    apidoc( apiOperation[T]( s"get${domainObject}ById" )
      summary s"Retrieve an existing ${domainObject} by id"
      parameter pathParam[String]( "id" ).description( s"The id of the ${domainObject} to be updated" ))) {
    getById( params("id")) map {
      case Some(domainObject: T) => Ok( domainObject )
      case _                     => NotFound( )
    }
  }
}

trait BatchCreateNew[T <: UUIDentifiable] {
  self: ScalatrestResource[T] with RepositoryCrudOperations[T] =>

  post( "/",
    apidoc( apiOperation[T]( s"createNew${domainObjects}" )
      summary s"Creates a collection of new ${domainObjects}"
      parameter bodyParam[T]( "body" ).description( s"The list of ${domainObjects} to be created" ) ) ) {
    postAll( wrap(parsedBody).extract[List[T]] ) map {
      case domainObjects: List[T] => Created( domainObjects )
      case _                      => InternalServerError( )
    }
  }

}

trait CreateNew[T <: UUIDentifiable] {
  self: ScalatrestResource[T] with RepositoryCrudOperations[T] =>

  post( "/",
    apidoc( apiOperation[T]( s"createNew${domainObject}" )
      summary s"Creates a new ${domainObject}"
      parameter bodyParam[T]( "body" ).description( s"The ${domainObject} to be created" ) ) ) {
    postSingle( parsedBody.extract[T]) map {
      case domainObject: T => Created( domainObjects )
      case _               => InternalServerError( )
    }
  }

}

trait ReplaceById[T <: UUIDentifiable] {
  self: ScalatrestResource[T] with RepositoryCrudOperations[T] =>

  put( "/:id",
    apidoc( apiOperation[T]( s"replace${domainObject}" )
      summary
      s"Replaces an existing ${domainObject} with the given one. " +
        s"The new object needs to be fully specified. Partial updates are supported using PATCH"
      parameter pathParam[String]( "id" ).description( s"The id of the ${domainObject} to be replaced" )
      parameter bodyParam[T]( "body" ).description( s"The new ${domainObject}" ) ) ) {

    replaceExisting( params("id"), parsedBody.extract[T] )
  }
}

trait PatchById[T <: UUIDentifiable] {
  self: ScalatrestResource[T] with RepositoryCrudOperations[T] =>
  patch( "/:id",
    apidoc( apiOperation[T]( s"patch${domainObject}" )
      summary s"Patches an existing ${domainObject} with a (partial) update. "
      notes s"All specified fields in the updating structure will be used to override existing fields."
      parameter pathParam[String]( "id" ).description( s"The id of the ${domainObject} to be updated" )
      parameter bodyParam[T]( "body" ).description( s"The ${domainObject} to be updated" ) ) ) {

    updateExisting( params( "id" ), parsedBody)
  }
}