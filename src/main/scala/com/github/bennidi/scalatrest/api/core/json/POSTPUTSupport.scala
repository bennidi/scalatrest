package com.github.bennidi.scalatrest.api.core.json

import javax.servlet.http.HttpServletRequest

import com.fasterxml.jackson.core.JsonParseException
import com.github.bennidi.scalatrest.api.core.{JsonSchemaReader, AbstractServletBase}
import com.github.bennidi.scalatrest.api.error.{InvalidJsonError, InvalidRequestFormatError}
import com.github.bennidi.scalatrest.core.json.{SchemaValidator, JsonUtils}
import com.github.bennidi.scalatrest.core.json.JsonUtils._
import com.github.bennidi.scalatrest.model.base.{ManifestProvider, UUIDentifiable}
import com.github.bennidi.scalatrest.persistence.core.GlobalExecutionContext
import org.json4s.JsonAST.JObject
import org.json4s.JsonAST.JString
import org.json4s.JsonAST.JValue
import org.json4s.JsonAST._
import org.json4s._
import org.scalatra.{BadRequest, MethodOverride}
import org.scalatra.json.JsonSupport

import scala.concurrent.Future

/**
 * Add POST/PUT/PATCH support including parsing and validation of json payloads based on
 * corresponding Json schema (see [[JsonSchemaReader]]
 *
 */
trait POSTPUTSupport[T <: UUIDentifiable] extends MethodOverride with JsonSchemaReader{
  self: AbstractServletBase with ScalatrestJsonSupport with GlobalExecutionContext with ManifestProvider[T] =>

  // Prevent Scalatra from parsing invalid json
  // (as it does not propagate exceptions or allow other error handling) :-/
  override protected def shouldParseBody(fmt: String)(implicit request: HttpServletRequest) = false

  before() {
    request.getMethod match {
      case "POST" | "PUT" | "PATCH" => {
        requestFormat match {
          case "xml" | "json" =>
          case _ => throw InvalidRequestFormatError(message = "Expected JSON format. Found " + requestFormat)
        }
        request(JsonSupport.ParsedBodyKey) = JsonUtils.parseJsonString(request.body)
        // Json Schema validation for all PUT and POST requests (excludes /search endpoint)
        if (request.getMethod != "PATCH" && !request.getRequestURI.endsWith("/search")){
          val validationResult = SchemaValidator.verify(parsedBody, readSchema(getSimpleClassName()))
          if (!validationResult.isEmpty) {
            // Communicate validation errors to the client
            throw InvalidJsonError ("The given json object did not pass schema validation: " +
              SchemaValidator.prettyPrint(validationResult))
          }
        }
      }
      case _ => // Ensure exhaustive match
    }
  }



}
