package com.github.bennidi.scalatrest.core.json
import com.github.fge.jsonschema.core.report.{ProcessingReport, ProcessingMessage}

import scala.collection.JavaConversions._

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jsonschema.main.JsonSchemaFactory

import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
 * Validator for JSON objects against JSON schema. Returns a validation result
 * to represent the outcome of the validation procedure. Clients may
 * decide how to respond to an error/errorless validation
 */
object SchemaValidator extends App {

  lazy val validator = JsonSchemaFactory.byDefault().getValidator


  private[this] def verify(instance: JsonNode, schema: JsonNode): SchemaValidationResult ={
    val processingReport = validator.validate(schema, instance)
    SchemaValidationResult(processingReport)
  }

  /**
   * Validates a JSON object against the given schema
   *
   * @param instance The JSON object to be validated
   * @param schema The schema that the instance need to conform to
   * @return A validation result that represents the outcome of the validation procedure
   */
  def verify(instance: JValue, schema: JValue): List[SchemaValidationResult] ={
    instance match {
      case instance:JObject => List(verify(asJsonNode(instance), asJsonNode(schema))) filter { result => !result.isOk()}
      case instance:JArray =>  instance.arr map {elem => verify(asJsonNode(elem), asJsonNode(schema))} filter { result => !result.isOk()}
      case _ => List.empty
    }
  }

  /**
   * Return a human readable representation of a list of validation results.
   * Can be used to communicate validation errors to [human] clients
   *
   */
  def prettyPrint(results:List[SchemaValidationResult]):String = {
    val messages = results map ( (result) => result.getErrorMessages().mkString(sep = "\n") )
    messages.mkString(", ")
  }

}

case class SchemaValidationResult(report:ProcessingReport){

  def isOk():Boolean = {
    report.isSuccess
  }

  def getErrorMessages():List[String] = {
    val messages = report.map { message => message.getMessage}
    messages.toList
  }


}