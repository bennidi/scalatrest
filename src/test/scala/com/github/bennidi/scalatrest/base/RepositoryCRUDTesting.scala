package com.github.bennidi.scalatrest.base

import com.github.bennidi.scalatrest.model.base.UUIDentifiable

/**
 * This trait provides generic CRUD tests that work with any domain model for which a repository
 * is available.
 *
 * Extend this trait to receive simple CRUD tests for your domain model.
 *
 */
trait PersistenceCRUD[T <: UUIDentifiable] extends PersistenceSpec[T] with TestFixtureProvider[T]{

  "A domain object of type " + getSimpleClassName() should " be the same when stored and retrieved" in {
    import com.github.bennidi.scalatrest.core.json.JsonUtils._
    import reactivemongo.extensions.Implicits._

    val domainObject = newRandom( )
    val origAsJson = toJson( domainObject )

    // compose async operations
    val createAndFetch = for {
      _ <- repository.save( domainObject )
      created <- ~repository.findById( domainObject.id )
    } yield ( created )

    whenReady( createAndFetch ) {
      case ( created ) =>
        val retrievedAsJson = toJson( created )
        created should equal( domainObject )
        retrievedAsJson should equal( origAsJson )
    }
  }

}
