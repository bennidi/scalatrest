package com.github.bennidi.scalatrest.api.core

import java.net.{URI, URL}
import javax.servlet.http.HttpServletRequest

import com.github.bennidi.scalatrest.api.core.OkHttp._
import com.github.bennidi.scalatrest.api.core.auth.JwtAuthFilter
import com.github.bennidi.scalatrest.api.core.auth.JwtAuthFilter._
import com.github.bennidi.scalatrest.core.json.JsonUtils
import org.json4s._
import org.json4s.JsonAST.{JNothing, JObject}
import org.scalatra.Created
import org.scalatra.{InternalServerError, BadRequest, Ok, Created}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scalaj.http.{HttpConstants, HttpRequest, Http}

/**
 * Expose support for Http calls. Each Http method is represented by its own case class which
 * can be executed as [[scalaj.http.HttpRequest]]. Execution is wrapped in a [[Future]] for
 * better integration with the other parts of the stack.
 */
trait HttpCallSupport{

  def Post(url:String, data:JValue = JObject())(implicit request:HttpServletRequest) = {
    OkHttp.Post(url, data).headers(Map(extractAuthHeader(request)))
  }

  def Put(url:String, data:JValue = JObject()) (implicit request:HttpServletRequest) = {
    OkHttp.Put(url, data).headers(Map(extractAuthHeader(request)))
  }

  def Patch(url:String, data:JValue = JObject())(implicit request:HttpServletRequest) = {
    OkHttp.Patch(url, data).headers(Map(extractAuthHeader(request)))
  }

  def Get(url:String)(implicit request:HttpServletRequest) = {
    OkHttp.Get(url).headers(Map(extractAuthHeader(request)))
  }

  def Stream(failOnError:Boolean = false):PartialFunction[ResponseWrapper, Any] = {
    OkHttp.Stream(failOnError)
  }

}

/**
 * Defines a common contract for any Http call object
 */
trait OkHttp extends LogSupport{

  def url:String

  private[this] var additionalHeaders:Map[String,String] = Map("content-type" -> "application/json; charset=UTF-8")

  def headers(headers:Map[String,String]):OkHttp = {
    this.additionalHeaders = additionalHeaders ++ headers
    this
  }

  /**
   * Execute the Http request and apply the transformation function to the result.
   *
   * @param transformFnc The function that will be applied to the result of the call
   * @param executionContext The executor for this request
   * @tparam S The type of result created by the transformation function
   * @return A future that will hold the value of type S, as soon as the future completed
   */
  def execute[S](transformFnc: (ResponseWrapper) => S = DefaultHandler)(implicit executionContext: ExecutionContext):Future[S] = {
    val request = Future[ResponseWrapper] {
      val cleanedUrl = cleanUrl(url)
      log.info("Downstream request to " + cleanedUrl)
      val response = buildRequest(cleanedUrl)
                     .headers(additionalHeaders)
                     .asString // <- executes the request (following the semantic of scalaj.http)
      response.body match {
        case "" => ResponseWrapper(response.code, response.body, response.headers)
        case _ => ResponseWrapper(response.code, JsonUtils.parseJsonString(response.body), response.headers)
      }
    }
    request onComplete {
      case Success(response) => response
      case Failure(exception) => (-1, exception)
    }
    request map transformFnc
  }

  def buildRequest(url:String):HttpRequest

  def cleanUrl(urlString:String): String ={
    val url = new URL(urlString)
    val uri = new URI(url.getProtocol, url.getUserInfo, url.getHost, url.getPort, url.getPath(), url.getQuery(), url.getRef());
    uri.toURL.toString
  }

}

/**
 * Defines a case class for each Http method and a wrapper for the
 * call response.
 */
object OkHttp{

  case class ResponseWrapper(code:Int, body:Any, headers:Map[String,String])

  // This partial will just stream the response through to the caller
  val StreamResponse:PartialFunction[ResponseWrapper, ResponseWrapper] = {
    case wrappedResponse => wrappedResponse
  }

  val DefaultHandler:PartialFunction[ResponseWrapper, Any] = {
    case ResponseWrapper(200, body, headers ) => Ok(body)
    case ResponseWrapper(201, body, headers) => Created(body, headers)
    case ResponseWrapper(400, body, headers) => BadRequest(body)
    case ResponseWrapper(-1, ex, headers) => ex
  }

  def Stream(failOnError:Boolean = false):PartialFunction[ResponseWrapper, Any] = {
    case response@ResponseWrapper(code,body:Throwable,headers) => if(failOnError && code == -1) throw body else response
    case response@ResponseWrapper(_,_,_) => response
  }



  case class Post(url:String,
                  data:JValue = JObject(), authorization:String = "") extends OkHttp{

    def buildRequest(url:String):HttpRequest = {
      val request = Http( url )
                    .postData( JsonUtils.render(data) )
      if (authorization.isEmpty) request else request.header("Authorization", authorization)
    }
  }

  case class Put(url:String,
                 data:JValue = JObject(), authorization:String = "") extends OkHttp{

    def buildRequest(url:String):HttpRequest = {
      val request = Http( url )
                    .postData( JsonUtils.render(data) )
                    .method("PUT")
      if (authorization.isEmpty) request else request.header("Authorization", authorization)
    }
  }

  case class Patch(url:String,
                   data:JValue = JObject()) extends OkHttp{

    def buildRequest(url:String):HttpRequest = {
      Http( url )
      .postData( JsonUtils.render(data) )
      .method("PATCH")
    }
  }

  case class Get(url:String) extends OkHttp{

    def buildRequest(url:String):HttpRequest = {
      Http( url )
    }
  }

}
