package com.github.bennidi.scalatrest.unit

import com.github.bennidi.scalatrest.base.FlatUnitTest
import com.github.bennidi.scalatrest.core.json.JsonUtils._
import org.json4s.JsonAST.JObject

class JsonDiffTest extends FlatUnitTest{

  "JsonDiffSupport" should "provide functionality for creating overlay views of Json objects" in {

    val orig = parseJson( """{
           "_id":5,
           "name":"orig",
           "some-numbers":[2,45],
           "retained-property-1":["elem1","elem2"],
           "nested":{
             "_id" : 43,
             "name": "nested-object",
             "values" : [{"_id":3}, {"_id":5}]
             },
           "related-ids":[2,45,34,23,7,5,3],
           "related-objects":[{
             "_id":23,
             "numbers":[2,45,34,23,3,5]
           }]
       }""" ).asInstanceOf[JObject]

    val update = parseJson( """{
           "name":"update",
           "newString":"aString",
           "newArray" : [3,4],
           "newObject" : { "id": 23},
           "some-numbers":[3],
           "nested":{
              "name": "nested-object-updated",
              "values" : [{"_id":3}]
              },
           "related-ids":[2,45],
           "related-objects":[{
             "_id":54,
             "numbers":[52,3,12,11,18,22]
           }]
       }""" ).asInstanceOf[JObject]

    val expectedMergeResult = parseJson( """{
           "_id" :5,
           "name":"update",
           "newString":"aString",
           "newArray" : [3,4],
           "newObject" : { "id": 23},
           "some-numbers":[3],
           "retained-property-1":["elem1","elem2"],
           "nested":{
              "_id" : 43,
              "name": "nested-object-updated",
              "values" : [{"_id":3}]
              },
           "related-ids":[2,45],
           "related-objects":[{
             "_id":54,
             "numbers":[52,3,12,11,18,22]
           }]
       }""" ).asInstanceOf[JObject]

    overlay(orig, update) should equal(expectedMergeResult)

  }


  "Transformation using Map[String,Any] as intermediate " should " be supported" in {

    val origJson = parseJson( """{
           "id" :5,
           "name":"update",
           "some-numbers":[],
           "nested":{
              "id" : 43,
              "name": "nested-object-updated",
              "values" : [{"id":3}]
              },
           "related-ids":[2,45,34,23,7,5],
           "related-objects":[{
             "id":54,
             "numbers":[52,3,12,11,18,22]
           }]
       }""" )
    val retransformed = toJson(origJson.extract[Map[String,Any]])
    retransformed should equal(origJson)
  }

}


