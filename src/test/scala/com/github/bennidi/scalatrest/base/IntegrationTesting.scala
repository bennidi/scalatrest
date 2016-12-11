package com.github.bennidi.scalatrest.base

import com.github.bennidi.scalatrest.api.core.LogSupport
import com.github.bennidi.scalatrest.core.di.{DIModules}
import com.github.bennidi.scalatrest.model.base.UUIDentifiable
import com.github.bennidi.scalatrest.persistence.RepositorySupport
import com.github.bennidi.scalatrest.persistence.core.GlobalExecutionContext
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest._
import org.scalatra.test.scalatest.ScalatraSuite

import scala.concurrent.Await

class IntegrationTests(suites: Seq[Suite]) extends Suites(suites:_*)
                                               with SuiteTaskSupport
                                               with EmbeddedMongoSupport
                                               with DIModules{

  addSetupTask(() => {
    log.info("Setting up environment variables")
    shutdownModules()
  })


  addTeardownTask(() => {
    log.info("Shutting down Scaldi modules")
    shutdownModules()
  })
}


/**
 * Base class for full stack integration tests.
 * This trait will startup a scalatra environment.
 */
trait IntegrationTesting extends ScalatraSuite
                                 with DIModules
                                 with GlobalExecutionContext
                                 with BeforeAndAfter
                                 with ScalaFutures
                                 with SuiteTaskSupport
                                 with LogSupport

/**
 * Use this trait to add setup and tear down tasks to your test suites.
 * Setup tasks are executed before the suite is run.
 * Tear down tasks are executed after the suite finished running
 *
 * Have a look at {@link TestFixtureExecutor} for an example
 */
trait SuiteTaskSupport extends Suite with BeforeAndAfterAll with PatienceConfiguration with LogSupport{

  // Setting the default configuration for waiting on async operations
  override implicit val patienceConfig = PatienceConfig(
    timeout = scaled( Span( 4000, Milliseconds ) ),
    interval = scaled( Span( 20, Milliseconds ) )
  )

  override protected def beforeAll( ): Unit = {
    log.info( s"Running setup tasks for ${getClass}" )
    for ( task <- setupTasks ){
      try {
        task( )
      }catch {
        case throwable:Throwable => throwable.printStackTrace()
      }
    }

    log.info( "Running suite setup tasks...done" )
    super.beforeAll( )
  }


  override protected def afterAll( ): Unit = {
    super.afterAll( )
    log.info( "Running suite teardown tasks..." )
    for ( task <- tearDownTasks.reverse ) task( )
    log.info( "Running suite teardown tasks...done" )
  }


  var setupTasks: Array[() => _] = Array( )
  var tearDownTasks: Array[() => _] = Array( )

  def addSetupTask( task: () => _ ) = {
    setupTasks = setupTasks :+ task
  }

  def addTeardownTask( task: () => _ ) = {
    tearDownTasks = tearDownTasks :+ task
  }

}

/**
 * Adds data setup and tear down tasks based on test fixture definitions
 * provided to this trait
 */
trait TestFixtureExecutor[T <: UUIDentifiable]{
  self: RepositorySupport[T]
    with TestFixtureProvider[T]
    with SuiteTaskSupport
    with LogSupport =>

  import scala.concurrent.duration._

  // Cleanup collection and seed with items defined in test fixture
  addSetupTask( ( ) => {
    log.info( "Initializing Collection" )
    val setup = for {
      _ <- repository.removeAll( )
      inserted <- repository.bulkInsert( existingItems )
    } yield inserted
    Await.result( setup, 5000 millis )
  } )

  // Cleanup collection
  addTeardownTask( ( ) => {
    log.info( "Clearing Collection" )
    Await.result( repository.removeAll( ), 5000 millis )
  } )

}

/**
 * Base trait for writing integration tests using FDD
 */
trait IntegrationSpec extends IntegrationTesting with FeatureSpec

/**
 * Base trait for writing tests using flat test specification
 */
trait IntegrationTest extends IntegrationTesting with FlatSpecLike with Matchers



/**
 * Convenience trait aggregating all functionality to write tests for persistence
 * aspect of the domain model.
 *
 * Extend this trait, provide a test fixture (required by self type of this trait)
 * and start writing tests
 *
 * @tparam T
 */
abstract class PersistenceSpec[T <: UUIDentifiable](implicit val manifest:Manifest[T]) extends IntegrationTesting
                                                                                               with RepositorySupport[T]
                                                                                               with ScalaFutures
                                                                                               with FlatSpecLike
                                                                                               with TestFixtureExecutor[T] {
  self: TestFixtureProvider[T] =>

}

