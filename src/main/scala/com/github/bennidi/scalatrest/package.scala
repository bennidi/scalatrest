package com.github.bennidi.scalatrest

/**
 *  =Model=
 *
 *  > Building JSON transformable, persistent data models using based on case classes.
 *
 *  Everything starts with a model of the things you want to store, retrieve and modify. The model is implemented
 *  using Scala's formidable case classes which bring the following features out-of-the-box
 *
 *    - Pattern matching (surprise! =8-O)
 *    - Support for implicit JSON<->Model transformations using json4s [[com.github.bennidi.scalatrest.core.json.JsonFormatProvider]]
 *    - Support for BSON<->Model transformations with Macros to auto-generate readers and writers
 *    - Support for CRUD repository (see [[com.github.bennidi.scalatrest.persistence]]) using BSON handlers with reactivemongo extension
 *
 *  Alltogether this allows to setup JSON transformable, persistent data models with just a few lines of code.
 *
 *  {{{
 *
 *    case class DataModel(name:String, description:String, submodels:List[SubModel])
 *    case class SubModel(untypedStructure:JObject)
 *
 *    object SomeDataModel {
 *        val SubModelBSONConverter = Macros.handler[SubModel]
 *        val DataModelBSONConverter = Macros.handler[DataModel]
 *    }
 *
 *    class ModelRepository( implicit ec: ExecutionContext ) extends BaseRepository[DataModel]( "collectionname" )
 *
 *  }}}
 *
 *  Have a look at [[com.github.bennidi.scalatrest.api.status.Account]] for a more comprehensive (and real) example.
 *
 *
 *  '''NOTES'''
 *    - Additional attributes contained in JSON representation of a model class will be ignored (silently dropped).
 *    - Missing attributes result in an error.
 *    - Json schema can be used to validate models. See the following classes
 *      [[com.github.bennidi.scalatrest.core.json.SchemaValidator]], [[com.github.bennidi.scalatrest.base.VerifyJsonSchema]]
 *
 *
 *
 *    [[com.github.bennidi.scalatrest.persistence Next: The repository]]
 */
package object model {

}

/**
 *  =Repository=
 *
 *  > Reactive persistence for models
 *
 *  Repositories are quite feature rich data access objects, as they provide a rich set of methods for retrieval and storage
 *  of model objects. They are built based on the  [[https://github.com/ReactiveMongo reactive driver and extensions]].
 *  Be sure to check out the [[https://github.com/ReactiveMongo/ReactiveMongo-Extensions/blob/master/guide/dsl.md query DSL]]
 *
 *  The repository is a one liner because all its functionality comes from a common base class
 *  [[com.github.bennidi.scalatrest.persistence.BaseRepository]] which provides CRUD and flexible search methods out-of-the-box.
 *
 *  {{{
 *
 *    class ModelRepository( implicit ec: ExecutionContext ) extends BaseRepository[DataModel]( "collectionname" )
 *
 *    val repository = new ModelRepository // this requires to have an implicit ExecutionContext in scope
 *
 *    // repository gives access to various finder methods, all of which return Futures
 *
 *    repository.findById (UUID)
 *    repository.findByIds (UUID1,UUID1)
 *    repository.findAll
 *    repository.findOne
 *    repository.findRandom
 *    repository.find ( "name"  $eq "yourName") // this is based on the BSONDSL
 *
 *  }}}
 *
 *  It only needs to hand through an implicit [[scala.concurrent.ExecutionContext]].
 *  Readers/Writers are to be '''located in the companion object''' of the model class.
 *
 *  '''NOTES'''
 *    - Put BSON macros for case classes into their companion object. This way they are automatically
 *    - Order of declaration of BSON Macros is important! Declare Macros of nested/referenced case classes
 *    before the macros for the classes that reference them
 *
 *    [[com.github.bennidi.scalatrest.model Previous: The Model]]  - [[com.github.bennidi.scalatrest.api Next: The REST resource]]
 */
package object persistence{

}

/**
 * = REST resource: Combining model and repository =
 *
 * > Exposing the model with a REST interface
 *
 * A REST resource combines model and repository and exposes a set of operations as publicly available Http(s)
 * endpoints. Exposing a full CRUD interface is a matter of mixing in a trait [[com.github.bennidi.scalatrest.api.core.CrudResource]].
 * This trait exposes GET,POST,PUT and PATCH for lists of and single model items. However, the trait
 * itself is just a composition of traits, each of which implements one single operation. So it is possible to
 * create REST resources that only expose a subset of the predefined operations by mixing in only the more
 * granular "sub-traits".
 *
 * {{{
 *
 *  class DataModelResource( implicit val manifest: Manifest[DataModel] ) // manifest needed by third-party libs
 *  extends CrudResource[DataModel] // Add full set of CRUD operations
 *           with JwtAuthFilter // Require a valid JSON Web Token for access of any route
 *           with AclProvider // Implementing JwtAuthFilter requires an ACL to be in scope
 *           with SearchEndpointSupport[DataModel] // Add the generic search endpoint "/search"
 *           with JsonSchemaProvider[DataModel] { // Add endpoint for related schema "/jsonschema"
 *
 *   // The repository that can handle DataModel objects (required by RepositorySupport)
 *   val repository = inject[DataModelRepository]
 *   // An access control list is maintained per REST resource
 *   val accessControl = inject[AccessControlList]("data-model")
 *   // The ResourceInfo groups some reusable information about this resource
 *   // NOTE the lazy val that is necessary in this case because of the way this value is accessed
 *   lazy val resourceInfo: RestResourceInfo = inject[RestResourceInfo]("data-model")
 *
 * }}}

 * Any REST resource uses each own [[com.github.bennidi.scalatrest.api.core.RestResourceInfo]] object to expose some meta information like pretty printed
 * model names and endpoint url to their environment. These infos are used in tests and other components like
 * [[com.github.bennidi.scalatrest.api.core.JsonSchemaProvider]].
 *
 * == Search and Alias-Support ==
 *
 * In addition to the CRUD endpoints any REST resource can be extended to provide a search endpoint that accepts
 * queries using mongodb syntax. This endpoint also support aliases. [[com.github.bennidi.scalatrest.persistence.Alias]] provide a means to reduce
 * queries to a flat list of parameters so that common queries do not have to be written repeatedly
 * but instead their alias can be used.
 *
 * == Documentation (Swagger) ==
 *
 * The documentation of each REST endpoint is written as Swagger API-Doc that is later automatically discovered
 * and served by an integrated Swagger endpoint [[com.github.bennidi.scalatrest.api.core.SwaggerApiDocProvider]]. The Swagger spec is generated from the manually written
 * documentation as well as a runtime inspection of the case class model.
 * It can be fed into the Swagger UI to have nicely laid out documentation with live access to the API.
 *
 * [[com.github.bennidi.scalatrest.model Previous: The Model]]  - [[com.github.bennidi.scalatrest.core.di Next: Dependency Injection]]
 *
 */
package object api {

}

/**
 * > Features regarding Dependency Injection and handling of JSON
 */
package object core {

}

/**
 * > Entry points for bootstrapping the application (Startup of Jetty Container, mounting of servlets etc.)
 */
package object boot{

}