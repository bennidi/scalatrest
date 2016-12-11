package com.github.bennidi.scalatrest.unit

import com.github.bennidi.scalatrest.base.{TestFixture, FlatUnitTest}
import com.github.bennidi.scalatrest.core.json.{JsonFormatProvider, JsonUtils, SchemaValidator}
import com.github.bennidi.scalatrest.api.core.JsonSchemaReader._

abstract class JsonSchemaVerificationTest extends FlatUnitTest with JsonFormatProvider{

  val schemas:List[String]

  "Available JSON schema files " should " be valid schemas according to JSON 4 (draft) meta schema" in {
    val metaschema = readSchema("jsonschemav4")
    schemas foreach ( (schema) => {
      log.info("Validating schema " + schema + " with metaschema")
      val result = SchemaValidator.verify(readSchema(schema), metaschema )
      if(!result.isEmpty)log.debug(SchemaValidator.prettyPrint(result))
      result.isEmpty should be (true)
    })
  }

  "Available JSON schema files " should " validate a full object of their respective type" in {
    schemas foreach ( (schema) => {
      val instance = JsonUtils
                     .load( schema + ".fixture.json" )
                     .extract[TestFixture].full
      log.info("Validating schema " + schema + " against instance")
      val result = SchemaValidator.verify(instance, readSchema(schema) )
      if(!result.isEmpty)log.debug(SchemaValidator.prettyPrint(result))
      result.isEmpty should be (true)
    })
  }


}
