package com.github.bennidi.scalatrest.unit

import com.github.bennidi.scalatrest.base.{TestFixtureProvider, FlatUnitTest}
import com.github.bennidi.scalatrest.core.json.JsonUtils._
import com.github.bennidi.scalatrest.model.base.{ManifestProvider, UUIDentifiable}

import scala.util.Random

/**
 * Generic test used to verify that any domainObject can be transformed into its Json representation
 * @param manifest
 * @tparam T
 */
abstract class DomainJsonTransformationTest[T <:UUIDentifiable](implicit val manifest: Manifest[T])
  extends FlatUnitTest with TestFixtureProvider[T] with ManifestProvider[T]{

  def randomDomainObject():T = {
    val items = seed()
    items(Random.nextInt(items.size))
  }


  "Each domain object" should "be (re)transformable" in {

    val domainObject = newRandom()
    val jsonEquivalent = toJson(domainObject)
    val reextracted = jsonEquivalent.extract[T]
    domainObject should equal(reextracted)
    jsonEquivalent should equal(toJson(reextracted))

  }

}
