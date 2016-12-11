package com.github.bennidi.scalatrest.base

import org.json4s.StringInput
import org.scalatest.FlatSpecLike
import org.scalatra.Ok
import reactivemongo.extensions.dao.BsonDao

abstract class DataImportFromFile[T](filename:String)(implicit manifest: Manifest[T]) extends IntegrationTesting with FlatSpecLike{

  val repo:BsonDao[T,String]


  "A data file: " + filename  should " be imported " in {

    import com.github.bennidi.scalatrest.core.json.JsonUtils._

    val source = scala.io.Source.fromFile("./src/test/resources/" + filename)
    val lines = try source.mkString finally source.close()

    val json = parseJson(new StringInput(lines))

    whenReady(repo.bulkInsert(json.extract[List[T]])){
      case _ => Ok()
    }
  }
}