package com.github.bennidi.scalatrest.api.core.json

import com.github.bennidi.scalatrest.api.core.LogSupport
import com.github.bennidi.scalatrest.core.json.JsonUtils
import com.github.bennidi.scalatrest.model.base.UUIDentifiable
import org.json4s._
import org.scalatra._
import org.scalatra.json.{JsonSupport, JValueResult, JacksonJsonSupport}

/**
 * Aggregation of all relevant traits necessary to support handling of JSON
 * in request and response.
 *
 * Supports post processing of JSON object based on [[RenderPipeline]].
 * Pattern matching and JSON AST transformation can be hooked in here
 * to support modification of the JSON repsonse before it is written to
 * the stream.
 */
trait ScalatrestJsonSupport extends JacksonJsonSupport with JsonPostProcessing {
  self: LogSupport =>

  // JacksonJsonSupport needs to know which Formats to use
  protected implicit val jsonFormats: Formats = JsonUtils.jsonFormats

  before( ) {
    contentType = formats( "json" ) // auto-format responses as json
    val requestId = UUIDentifiable.generateId()
    request ("rid") = requestId
    log.info( request.getMethod + " "
      + request.getServletPath + ( if ( request.getPathInfo == null ) "" else request.getPathInfo )
      + " [params: " + ( if ( params isEmpty ) "None" else params ) + "]"
      + " [payload: " + ( if ( request.body isEmpty ) "None"
    else request.body.substring( 0, Math.min( request.body.length, 50 ) ).trim + "...trimmed" ) + "](" + requestId + ")")
  }
}

/*
* Pattern matching and JSON AST transformation can be hooked in here
* to support modification of the JSON response before it is written to
* the stream.
*/
object JsonPipeline {

  val root: PartialFunction[Any, Any] = {
    case array: JArray => processRootElement( array )
    case obj: JObject  => processRootElement( obj )
  }

  def processRootElement( target: JValue ): JValue = {
    target match {
      case target: JArray  => JArray( target.children map {processRootElement( _ )} )
      case target: JObject => target //mapField ( _idToId orElse catchAll )
      case any: JValue     => any
    }
  }

  val _idToId: PartialFunction[JField, JField] = {
    case ("_id", any) => ("id", any)
  }

  val catchAll: PartialFunction[JField, JField] = {
    case tuple: JField => tuple
  }


}

/**
 * Add custom processing step to the [[RenderPipeline]]
 * This is an extension hook for generic processing steps applied to the
 * Json AST.
 */
trait JsonPostProcessing extends ScalatraBase {
  self: JsonSupport[_] =>

  override protected def renderPipeline: RenderPipeline = jsonPipeline orElse super.renderPipeline

  private[this] def jsonPipeline: RenderPipeline = {
    case a: JValue if JsonPipeline.root.isDefinedAt( a ) => super.renderPipeline( JsonPipeline.root.apply( a ) )
    case a: JValue                                       => super.renderPipeline( a )

  }


}
