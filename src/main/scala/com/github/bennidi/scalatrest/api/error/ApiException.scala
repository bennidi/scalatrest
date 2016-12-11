package com.github.bennidi.scalatrest.api.error

import org.json4s.JsonAST.JObject
import org.json4s._
import org.scalatra.{BadRequest, InternalServerError}

class ApiException(message: String = null, cause: Throwable = null, code:Int = ErrorCode.Internal)
  extends RuntimeException(message  + " (code:" + code + ")", cause){

  def errorMessage( msg: String ) = JObject(JField( "message", JString( msg) ))

  def toHttpResponse() = {
    code match {
      case ErrorCode.InvalidJson =>
        BadRequest( reason = "Malformed JSON",
          body = errorMessage( getMessage ),
          headers = Map( "X-Error-Type" -> "Malformed JSON" ) )
      case ErrorCode.SchemaValidationError =>
        BadRequest( reason = "The provided JSON did not match the corresponding schema",
          body = errorMessage( getMessage ) ,
          headers = Map( "X-Error-Type" -> "Schema validation error" ) )
      case other =>
        InternalServerError( reason = "Internal Server error",
          body = errorMessage( getMessage ) ,
          headers = Map( "X-Error-Type" -> "Internal Server error" ) )
    }
  }

}

object InvalidRequestFormatError{

  def apply(message:String):ApiException = {
    new ApiException(message = message, code = ErrorCode.InvalidRequestFormat)
  }

}

object InvalidJsonError{

  def apply(message:String):ApiException = {
    new ApiException(message = message, code = ErrorCode.InvalidJson)
  }

}

object ErrorCode{

  val InvalidRequestFormat = 100
  val InvalidJson = 110
  val SchemaValidationError = 111
  val Technical = 400
  val Internal = 410

}