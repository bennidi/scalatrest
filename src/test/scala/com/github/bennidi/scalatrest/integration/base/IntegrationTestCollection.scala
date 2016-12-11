package com.github.bennidi.scalatrest.integration.base

import com.github.bennidi.scalatrest.api.core.OkHttp.{Post, _}
import com.github.bennidi.scalatrest.api.core.RestResourceInfo
import com.github.bennidi.scalatrest.api.core.auth.JwtAuthFilter.AuthHeader
import com.github.bennidi.scalatrest.api.resources.AccountResource
import com.github.bennidi.scalatrest.api.model.Account
import com.github.bennidi.scalatrest.base._
import com.github.bennidi.scalatrest.core.json.JsonUtils
import org.json4s.JsonAST.JArray
import org.scalatest.{DoNotDiscover}
import reactivemongo.api.DB


@DoNotDiscover
class DependencyInjectionTest extends IntegrationTest {

  val db: DB = inject[DB]

  "An integration test should " should " have access to dependencies defined in injector modules" in {
    db should not be ( null )
  }

}



case class Token(role:String, valid:Boolean, token:String)
@DoNotDiscover
class JwtAuthorizationTest extends RestSpec[Account] {

  val resource: AccountResource = inject[AccountResource]
  val resourceInfo = inject[RestResourceInfo]( "accounts" )

  mount( resource, resourceInfo.urlBase )

  feature( "Exposed endpoints can be secured using JWT (Json Web Tokens)" ) {

    info( "Web Tokens are provided to endpoints using request header (Authorization)" )
    info( "Web Tokens are encrypted and contain expiration date and user role information" )

    val tokens = JsonUtils.load("Tokens.json").extract[List[Token]]
    val invalid = tokens collect { case tok@Token(_,false,_) => tok }
    val valid = tokens collect { case tok@Token(_,true,_) => tok }

    scenario( "Empty JWT authorization header" ) {

      When( "an HTTP request without token is sent" )
      Then( "a 401(Bad Request) is returned" )

      get( uri = s"${resourceInfo.urlBase}" , headers = Map.empty) {
        status should equal( 401 )
      }
    }

    scenario( "Expired token" ) {

      When( "an HTTP request with invalid token is sent" )
      Then( "a 201(unauthorized) is returned" )

      get( uri = s"${resourceInfo.urlBase}",
        headers = Map(AuthHeader -> invalid(0).token) ) {
        status should equal( 201 )
      }
    }

    scenario( "Valid token" ) {

      When( "an HTTP request is sent with the token" )
      Then( "a 200 is returned" )

      get( uri = s"${resourceInfo.urlBase}",
        headers = Map(AuthHeader -> s"""Bearer ${valid(0).token}""") ) {
        status should equal( 200 )
      }
    }
  }

}

@DoNotDiscover
class InterServiceCallTest extends WiremockedSpec {

  val resourceStatusUrl = inject[String]( "services.endpoints.accounts" )

  addWiremockFixture( "accounts.json" )

  feature( "A client can do inter service calls using Http protocol" ) {

    info( "Clients need to be able to access others services using their exposed Http API" )

    scenario( "The client posts some JSON data to an endpoint to retrieve a result (JSON)" ) {

      Given( "An endpoint mounted as status-resource" )

      When( "an HTTP POST is sent" )
      Then( "a 200 is returned containing the result as Json" )

      whenReady( Post( url = s"$resourceStatusUrl/search" ) execute StreamResponse ) {
        case ( response ) => {
          response.code should equal( 200 )
        }
      }
    }

    scenario( "The clients calls an endpoint using GET to retrieve a result (JSON)" ) {

      Given( "An endpoint mounted as status-resource" )

      When( "an HTTP POST is sent" )
      Then( "a 200 is returned containing the result as Json" )

      whenReady( Get( url = resourceStatusUrl ) execute StreamResponse ) {
        case ( response ) => {
          response.code should equal( 200 )
          response.body should equal( JArray( List( ) ) )
        }
      }
    }

    scenario( "The clients calls an endpoint using PATCH to update a result (JSON)" ) {

      Given( "An endpoint mounted as status-resource" )

      When( "an HTTP POST is sent" )
      Then( "a 200 is returned containing the result as Json" )


      val item = Account(name = "Healthy", code = 100)
      val update = for {
        posted <- Post( url = resourceStatusUrl, data = JsonUtils.toJson( item ) ) execute StreamResponse
        patched <- Patch( url = resourceStatusUrl + item.id( ) ) execute StreamResponse
      } yield ( patched )

      whenReady( update ) {
        case ( response ) => {
          response.code should equal( 200 )
        }
      }
    }

  }

}

