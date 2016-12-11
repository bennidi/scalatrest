package com.github.bennidi.scalatrest.api.core.auth

import java.security.{PrivateKey, PublicKey}
import javax.servlet.http.HttpServletRequest

import com.github.bennidi.scalatrest.api.core.LogSupport
import com.github.bennidi.scalatrest.api.core.auth.JwtAuthFilter.AuthHeader
import com.github.bennidi.scalatrest.api.error.ApiException
import com.github.bennidi.scalatrest.core.di.DIModules
import com.github.bennidi.scalatrest.core.json.JsonUtils
import org.apache.http.HttpHeaders
import org.json4s.JsonAST.{JString, JValue, JNothing, JObject}
import org.scalatra.ScalatraBase
import pdi.jwt.{JwtAlgorithm, Jwt}

import scala.util.Success
import scala.util.matching.Regex

/**
 * Defines dependency for [[AccessControlList]] such that components that need such
 * an object, i.e. [[JwtAuthFilter]] may state this requirement
 */
trait AclProvider {
  val accessControl: AccessControlList
}

/**
 * Apply authorization to routes. Any restricted route will only be accessible with a valid token
 * that provides the correct roles. Any route has restricted access unless explicitly unlocked in
 * its [[AccessControlList]].
 * 
 * Exposes "uid" in request params.
 */
trait JwtAuthFilter extends ScalatraBase with DIModules with LogSupport {
  self: AclProvider =>

  private[this] val jwtManager = inject[JwtManager]

  before( ) {
    // Access control is determined by matching access path with regex from ACL config
    val accessPath = request.getServletPath + ( if ( request.getPathInfo == null ) "" else request.getPathInfo )
    val isRestricted = accessControl.isRestricted( accessPath )
    // Authorization information is sent with request headers
    val authHeader = Option( request.getHeader( AuthHeader ) )

    val token = authHeader match {
      case Some( x ) => Some(jwtManager.decode(x.substring(7)))
      case _         => None
    }
    // Check the token now
    token match {
      case Some( x ) if isRestricted => {
        log.debug( s"${accessPath} has restricted access" )
        if ( !jwtManager.containsRequiredRoles( x ) )
          halt( status = 201 )
        else
          request("uid") = jwtManager.getUID(x)
      }
      // Expose uid in request such that it can be passed on in downstream calls
      case Some(x) => request("uid") = jwtManager.getUID(x)
      case None if isRestricted =>
        log.debug(s"Access to restricted endpoint without token")
        halt( status = 401 )
      case _ =>
    }
  }

  def getUID():String = {
    request("uid").asInstanceOf[String]
  }
}

/**
 * Wrapper around JSON Web Token implementation
 */
class JwtManager extends LogSupport with DIModules {

  private[this] val publicKey = inject[PublicKey]
  private[this] val secretKey = inject[PrivateKey]

  /**
   * Checks the token for valid roles
   */
  def containsRequiredRoles( jwtToken: JValue ): Boolean = {
    // decode is either JObject (valid) JNothing (invalid)
    jwtToken match {
      case token: JObject => {
        val userRole = ( token \\ "roles" ).values.toString
        userRole == "ROLE_USER" || userRole == "ROLE_ADMIN"
      }
      case _              => false
    }
  }

  /**
   * Encode given claims as Web Token
   *
   * @param claims - Claims as Json String
   * @return
   */
  def encode (claims: String): String = {
    Jwt.encode(claim = claims, key = secretKey, algorithm = JwtAlgorithm.RSASHA256)
  }

  /**
   * @see #encode(String)
   */
  def encode (details: JObject): String = {
    encode (JsonUtils.render(details))
  }

  /**
   * Decode a token string and return its claims as JSON object
 *
   * @param token The token to decode
   * @return The decoded JObject or JNothing if token could not be decoded
   */
  def decode( token: String ): JValue = {
    val trimmedToken = "[...]" + token.substring( token.length - 50, token.length - 1 )
    Jwt.decode( token, publicKey ) match {
      case Success( jsonJwt: String ) => {
        log.debug( s"Token ${trimmedToken} accepted" )
        JsonUtils.parseJsonString( jsonJwt )
      }
      case _                          => {
        log.debug( s"Token ${trimmedToken} rejected" )
        JNothing
      }
    }
  }

  def getUID(token: String ): String = {
    getUID(decode(token))
  }

  def getUID(token: JValue ): String = {
    ( token \ "sub" ) match {
      // Every token starts with "Bearer " <- this needs to be removed
      case JString(sub) => sub
      case _              => throw new ApiException("No uid found in token")
    }
  }
}

object JwtAuthFilter {

  val AuthHeader = HttpHeaders.AUTHORIZATION

  def extractAuthHeader( httpServletRequest: HttpServletRequest ): (String, String) = {
    (AuthHeader, httpServletRequest.getHeader( AuthHeader ))
  }
}

/**
 * The access control list is a simple regex based route matcher
 * that is populated from a regex string sourced from a config file. The regular expressions
 * are treated as a whitelist for routes. Any route that matches one of the regex will not
 * be considered to have restricted access.
 *
 * The [[AccessControlList]] from [[com.github.bennidi.scalatrest.api.resources.AccountResource]] is
 * an example of how
 *
 * @param urlPatterns The regular expression that identifies all unrestricted routes
 */
class AccessControlList( urlPatterns: String ) extends LogSupport {

  val whitelist: Regex = ( "(" + urlPatterns.replace( ",", "|" ) + ")" ).r

  def isRestricted( url: String ): Boolean = {
    url match {
      // Apply the regex (if match then url is whitelisted and not restricted)
      case whitelist( c ) => false
      case _              => true
    }
  }
}

