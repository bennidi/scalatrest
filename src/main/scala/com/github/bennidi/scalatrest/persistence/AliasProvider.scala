package com.github.bennidi.scalatrest.persistence

import com.github.bennidi.scalatrest.api.error.ApiException
import com.github.bennidi.scalatrest.core.json.JsonUtils._
import org.json4s.JsonAST.JObject

/**
 * Aliases represent convenience abstractions over actual BSON queries. There purpose is to
 * simplify usage of the /search endpoint, see [[com.github.bennidi.scalatrest.api.core.crud.SearchEndpointSupport]]
 *
 *
 * @param name The public name of the alias. The name is used to identify an alias within a given set
 * @param query The query string that is populated from this aliases template to produce a valid BSON query
 * @param template The template defines all variable parts of the query.
 * @param description A human readable description of the purpose of this Alias
 */
case class Alias( name: String, query: String, template: String, description: String ) {

  def explain( ): String = {
    template + " -> " + description
  }

  // Build a mongodb query from the template string
  def applyReplacments( replacements: Map[String, Any] = Map( ) ): String = {
    replacements.foldLeft( query )(
      ( s: String, x: (String, Any) ) => ( "#\\{" + x._1 + "\\}" ).r.replaceAllIn( s, x._2.toString ) )
  }

  /**
   * Take the query string and replace all variable occurrences with a value taken from the map
   * of replacements.
   * @param replacements The values to use for population of the query
   * @return The BSON query from
   */
  def buildQuery( replacements: Map[String, Any] = Map( ) ):JObject = {
    parseJson(applyReplacments(replacements)).asInstanceOf[JObject]
  }
}

/**
 * Manages aliases and resolution thereof.
 */
trait AliasProvider{

  // Store aliases by their name
  val aliases: scala.collection.mutable.Map[String, Alias] = scala.collection.mutable.Map.empty

  /**
   * Add aliases to this AliasProvider
   *
   * @param aliasToAdd The aliases that should be added. Overwrites existing with new
   */
  def addAliases(aliasToAdd:Alias*):AliasProvider = {
    aliasToAdd foreach { (alias) => aliases += (alias.name -> alias)}
    this
  }

  /**
   * Resolve an alias to its corresponding BSON query
   *
   * @param aliasedQuery A map containing that alias name and all of its parameters
   * @return The translated BSON query
   */
  def resolveAlias( aliasedQuery: Map[String, Any] = Map( ) ): JObject = {
    val alias = aliases.get(aliasedQuery( "$alias" ).asInstanceOf[String])
    alias match {
      case Some(alias) => alias.buildQuery( aliasedQuery - "$alias" )
      case None => throw new ApiException(message = "Unknown alias " + aliasedQuery.mkString(", "))
    }
  }


}
