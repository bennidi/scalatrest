package com.github.bennidi.scalatrest.api.core.crud

import com.github.bennidi.scalatrest.api.core.json.ScalatrestJsonSupport
import com.github.bennidi.scalatrest.api.core.{JObjectToBson, ResourceInfoProvider, AbstractServletBase, ApiDocumentationSupport}
import com.github.bennidi.scalatrest.model.base.{ManifestProvider, UUIDentifiable}
import com.github.bennidi.scalatrest.persistence.{AliasProvider, RepositorySupport}
import org.json4s.JsonAST.{JObject, JString}
import reactivemongo.bson.{BSON, BSONDocument}

import scala.concurrent.Future

/**
 * Adds support for Mongo query based finders to REST resources.
 * The endpoint /search can be used to POST queries written in Mongo query syntax.
 * Queries will be sent to the persistence layer for execution.
 *
 * Example:
 *
 * {{{
 *   val query = ("name" -> ("$regex" -> ".*Allianz.*"))
 *   Post( "/search", body = query)
 * }}}
 *
 */
trait SearchEndpointSupport[T <: UUIDentifiable] extends AliasProvider{
  self: AbstractServletBase
    with ScalatrestJsonSupport
    with ApiDocumentationSupport with ResourceInfoProvider
    with RepositorySupport[T] with ManifestProvider[T] with JObjectToBson=>

  before( "*/search" ) {
    // Resolve potential alias
    val alias: Map[String, Any] = parsedBody match {
      // if body contains reference to alias {"$alias" : "name" ...<params>}
      case JObject( List( ("$alias", JString( alias )), _ ) ) => parsedBody.extract[Map[String, Any]]
      case _                                                  => Map( )
    }
    val query =
      if ( alias.isEmpty ) parsedBody.asInstanceOf[JObject]
      else resolveAlias( alias )

    request( "bsonquery") =  BSON.write( query )

  }

  post( "/search",
    apidoc( apiOperation[List[T]]( "findByQuery" )
      summary s"Find all ${domainObjects} matching a given query or alias. " +
      s"You can GET available aliases from /aliases"
      parameter bodyParam[Option[String]]( "body" ).description( "A query or alias using mongo style syntax" ) ) ) {

    findMatching( )

  }

  get( "/aliases",
    apidoc( apiOperation[List[T]]( "listAliases" )
      summary s"Show all available aliases" ) ) {
    aliases.values
  }


  def BsonQuery( ): BSONDocument = {
    request( "bsonquery" ).asInstanceOf[BSONDocument]
  }

  def findMatching( )( implicit manifest: Manifest[T] ): Future[List[T]] = {
    repository.findAll( selector = BsonQuery( ) )
  }

}
