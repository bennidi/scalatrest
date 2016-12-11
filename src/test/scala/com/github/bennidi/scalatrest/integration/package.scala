package com.github.bennidi.scalatrest

/**
 * Integration tests provide automatic setup and teardown of infrastructure necessary to test
 * REST resources or other components that require access to a data store or more complex environments
 * than simple unit tests.
 *
 *  - [[com.github.bennidi.scalatrest.base.RestSpec]] [[com.github.bennidi.scalatrest.base.PersistenceSpec]] and [[com.github.bennidi.scalatrest.base.IntegrationSpec]]
 *    are the most common base classes to be used
 *  - [[com.github.bennidi.scalatrest.base.SuiteTaskSupport]] provides functionality to run setup and tear down tasks before each suite.
 *  - [[com.github.bennidi.scalatrest.base.EmbeddedMongoSupport]] will start/stop a MongoDB instance
 *  - [[com.github.bennidi.scalatrest.base.TestFixtureExecutor]] takes care of preparing the datastore with data from
 *    [[com.github.bennidi.scalatrest.base.TestFixture]] as provided by [[com.github.bennidi.scalatrest.base.TestFixtureProvider]]
 *  - [[com.github.bennidi.scalatrest.base.WireMockSupport]] starts a [[http://wiremock.org/ Wiremock]] server such that interaction
 *    with external services can be mocked and verified
 *  -
 *
 * == Repository Tests ==
 * For verification that the persistence aspect of the data model functions correctly the 
 * [[com.github.bennidi.scalatrest.base.PersistenceCRUD]] trait provides a set of simple tests that store and retrieve domain
 * model objects.
 * 
 * Simply extend this trait to have a first verification that your data model can correctly be persisted.
 * 
 * {{{
 *   
 *   @DoNotDiscover
 *   class DataModelRepositoryTest extends PersistenceCRUD[DataModel]{
 *
 *     override val repository: BaseRepository[DataModel] = inject[DataModelRepository]
 *     // DO NOT FORGET to add this test to the aggregating integration tests suite
 *    }
 *   
 * }}}
 * 
 * 
 * == Rest Resource Tests ==
 *
 * The equivalent to [[PersistenceCRUD]] for Rest resources is [[GenericCRUDSpec]]. Extending that trait
 * will provide your resource tests with a comprehensive set of CRUD tests to verify correct functionality
 * of your Rest resources CRUD interface. In fact, you get 100 % test coverage to start from.
 * 
 * If your Rest resource does not provide all operations from [[com.github.bennidi.scalatrest.api.core.CrudResource]] then
 * using [[GenericCRUDSpec]] will cause test failures as operations are not exposed.
 * However, [[GenericCRUDSpec]] follows the same granularity as the [[com.github.bennidi.scalatrest.api.core.CrudResource]]
 * trait such that tests can be composed from single operation tests, such as [[com.github.bennidi.scalatrest.base.ReadSingleItemSpec]]
 *
 * {{{
 *   
 *   @DoNotDiscover
 *   class DataModelResourceSpec extends GenericCRUDSpec[DataModel]{
 *
 *     lazy val resourceStatusResource = inject[DataModelResource]
 *     lazy val repository = inject[DataModelRepository]
 *     lazy val resourceInfo: RestResourceInfo = inject[RestResourceInfo]("datamodel")
 *
 *     addServlet( resourceStatusResource, endPointBaseUrl + "/\*" )
 *
 *   }
 *   
 *   
 * }}}
 * 
 *
 * == Fixtures ==
 *
 * Integration tests depend on the correct data to be around. Because the entire stack is built around
 * producing and consuming JSON, it is just obvious that test fixtures are written in JSON as well.
 * For each data model root class (the one handled by the repository and exposed as rest resource)
 * a fixture is expected to be in place. Fixtures follow a naming convention and are looked up from classpath.
 * 
 * [[com.github.bennidi.scalatrest.base.TestFixtureProvider]] encodes these rules and provides ability to load
 * [[com.github.bennidi.scalatrest.base.TestFixture  fixtures]] from local file system. Any integration test that deals with
 * persistent data usually requires the test to implement [[TestFixtureProvider]].
 *
 * [[TestFixtureProvider]] in turn requires a Manifest to be available, which it uses to determine the
 * name of the fixture to load.
 *
 *
 * = NOTES =
 * 
 *   - Integration Tests are aggregated in [[com.github.bennidi.scalatrest.integration.AllIntegrationTests]] to provide one
 *   suite as the root for test running. This is necessary to have stable test setup with embedded mongo
 *   - Project specific integration tests need to be added to [[com.github.bennidi.scalatrest.integration.ProjectIntegrationTests]]
 *
 *
 */
package object integration {

}
