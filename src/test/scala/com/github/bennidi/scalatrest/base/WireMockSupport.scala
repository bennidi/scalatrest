package com.github.bennidi.scalatrest.base

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.common.{ConsoleNotifier, LocalNotifier}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.github.bennidi.scalatrest.api.core.OkHttp.{Get, Post}
import com.github.bennidi.scalatrest.core.di.DIModules
import com.github.bennidi.scalatrest.core.json.JsonUtils
import com.github.bennidi.scalatrest.persistence.core.GlobalExecutionContext
import org.json4s.JsonAST.{JValue, JArray, JObject}
import org.scalatest.concurrent.ScalaFutures

/**
 * Integration with WireMock {@see http://wiremock.org/}
 *
 * WireMock allows to define Http endpoints that behave according to stubs
 * that are defines as JSON fixtures. This allows to mock external services using with very fine
 * grained control over responses (returned status code + header and body content).
 *
 * Stubs are added as SuiteSetupTasks and expected to
 * reside in /src/test/resources/wiremock/__mappings
 *
 * See [[com.github.bennidi.scalatrest.integration.base.InterServiceCallTest]] for a usage example
 *
 * {{{
 *
 *   class SomeWiremockedTest extends WiremockedSpec {
 *
 *    addWiremockFixture( "accounts.json" )
 *
 *    feature( "Wiremock is started automatically from WiremockedSpec" ) {
 +
 *      info( "The fixture will be deployed before any test runs" ) {
 *
 *        // run test code here
 *
 *      }
 *
 *    }
 *
 * }}}
 *
 *
 * Issues
 * ======
 * WireMock is started as a jetty container but has outdated dependencies. It adds a 2.5 version
 * of the servlet-api to the classpath. This potentially causes conflicts in resolution in test
 * execution (scope of WireMock dependency is "test")
 *
 */
trait WireMockSupport extends GlobalExecutionContext{

  self: SuiteTaskSupport =>

  private[this] var server:WireMockServer = null
  private[this] var stubs:List[String] = List.empty

  addSetupTask( ( ) => {
    log.info("Starting WireMock")
    server = new WireMockServer( wireMockConfig()
                                 .port(8090).usingFilesUnderClasspath("src/test/resources/wiremock")
                                 .notifier(new ConsoleNotifier(true)) )
    server.start()
    stubs foreach addStub
  } )

  // Cleanup collection
  addTeardownTask( ( ) => {
    log.info("Stopping WireMock")
    server.stop()

  } )


  def WireMock():WireMockServer = server


  def addWiremockFixture(resourceName:String): Unit ={
    stubs = stubs ++ List(resourceName)
  }

  def addStub(resourceName:String) = {
    log.info("Deploying Wiremock fixture " + resourceName)
    import scala.concurrent.duration._
    val stub = JsonUtils
               .parseJson( getClass.getClassLoader.getResourceAsStream( "wiremock/__mappings/" + resourceName ) )
    val addMapping = (stub:JValue) => server.addStubMapping(StubMapping.buildFrom(JsonUtils.render(stub)))
    stub match {
      case JArray( list ) => list foreach addMapping
      case stub: JObject  => addMapping( stub )
      case _              =>
    }
  }

  @deprecated // Use WireMock().verify()... directly
  def verifyOneJsonPost(resourceName:String, value: String, authorization: String) = {
    server.verify(1,postRequestedFor(urlEqualTo(resourceName))
                    .withHeader("Content-Type", containing("application/json"))
                    .withHeader("Authorization", equalTo(authorization))
                    .withRequestBody(equalToJson(value)));
  }

  @deprecated // Use WireMock().verify()... directly
  def verifyOneJsonPut(resourceName:String, value: String, authorization: String) = {
    server.verify(1, putRequestedFor(urlEqualTo(resourceName))
                     .withHeader("Content-Type", containing("application/json"))
                     .withHeader("Authorization", equalTo(authorization))
                     .withRequestBody(equalToJson(value)));
  }

  @deprecated // Use WireMock().verify()... directly
  def verifyOneJsonPatch(resourceName:String, value: String, authorization: String) = {
    server.verify(1, patchRequestedFor(urlEqualTo(resourceName))
      .withHeader("Content-Type", containing("application/json"))
      .withHeader("Authorization", equalTo(authorization))
      .withRequestBody(equalToJson(value)));
  }

  @deprecated // Use WireMock().verify()... directly
  def verifyOneJsonGet(resourceName:String, authorization: String) = {
    server.verify(1, getRequestedFor(urlEqualTo(resourceName))
      .withHeader("Authorization", equalTo(authorization)));
  }
}

/**
 * Mixin this trait will startup a WiremockServer before your tests are run
 */
trait WiremockedSpec extends FeatureSpec
                             with ScalaFutures with SuiteTaskSupport with GlobalExecutionContext
                             with WireMockSupport with DIModules