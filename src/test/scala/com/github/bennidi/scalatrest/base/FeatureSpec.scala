package com.github.bennidi.scalatrest.base

import org.scalatest._



/**
 * Support for writing tests in feature style specification
 * Read: http://www.scalatest.org/getting_started_with_feature_spec
 * and find example template below
 */
trait FeatureSpec extends FeatureSpecLike with GivenWhenThen with Matchers


class FeatureSpecSetupTest extends FeatureSpec{

  feature("Developers can write tests using behaviour driven like spec interface") {

    info("As a programmer")
    info("I want to be able to write tests with feature spec style")
    info("So that I can produce nicely readable tests that also non-technical people can understand")

    scenario("a non-empty test is specified with tautological assertions") {

      Given("a fake test scenario with assertions written using should syntax")
      When("the assertions all specify tautologies")
      Then("the the test will pass")
      And("the console output will be green")

      "aString" should equal("aString")
      Array(1,2,3) should contain(1)
    }

    scenario("an empty test is specified to outline the requirements of a feature") {

      Given("a product owner who wants a feature to be implemented")
      When("the product owner has the feature specification ready")
      Then("he/she can write this specification as executable code that outlines a (pending) test")
      And("the specification will show as pending until implemented by a developer")
      pending
    }
  }

}