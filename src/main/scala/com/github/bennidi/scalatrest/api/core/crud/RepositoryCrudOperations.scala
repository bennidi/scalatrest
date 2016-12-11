package com.github.bennidi.scalatrest.api.core.crud

import com.github.bennidi.scalatrest.core.json.JsonUtils._
import com.github.bennidi.scalatrest.model.base.UUIDentifiable
import com.github.bennidi.scalatrest.persistence.RepositorySupport
import org.json4s.JsonAST.JValue
import org.scalatra.json.JacksonJsonSupport

import scala.concurrent.Future

/**
 * Add support for CRUD operations to REST resources. This is a thin wrapper around the persistence layer
 * that returns either [[org.scalatra.ActionResult]] or [[Future]] to represent the outcome of the operation.
 */
trait RepositoryCrudOperations[T <: UUIDentifiable] {
  self: JacksonJsonSupport
    with RepositorySupport[T] =>

  import reactivemongo.extensions.Implicits._

  /**
   * Extract the domain object representation from the request body.
   * @return A newly constructed instance of type <T>
   */
  def extract( )( implicit manifest: Manifest[T] ): T = {
    parsedBody.extract[T]
  }

  /**
   * Retrieve a domain object by id.
   */
  def getById(id:String )( implicit manifest: Manifest[T] ):Future[Option[T]] = {
    repository.findById( id )
  }

  /**
   * Return a list of all domain objects
   */
  def getAll( )( implicit manifest: Manifest[T] ):Future[List[T]] = {
    repository.findAll( )
  }

  /**
   * Persist a single or multiple domain objects. Transparently wraps single items
   * in an array.
   */
  def postAll( items:List[T] )( implicit manifest: Manifest[T] ):Future[List[T]] = {
    val ids = items.collect { case item => item.id( ) }
    for {
      _ <- repository.bulkInsert( items )
      created <- repository.findByIds( ids: _* )
    } yield created
  }

  /**
   * Persist a single domain object.
   */
  def postSingle( item:T )( implicit manifest: Manifest[T] ):Future[T] = {
    for {
      _ <- repository.save( item )
      created <- ~repository.findById( item.id )
    } yield created
  }

  /**
   * Updates an existing object by merging the fields specified in the update into
   * the existing object. See {@link JsonUtils.merge} for reference.
   *
   * @return The updated object
   */
  def updateExisting( id:String, update:JValue)( implicit manifest: Manifest[T] ) = {
    for {
      existing <- ~repository.findById( id )
      merged <- Future{ merge[T]( existing, update ) }
      storeOperation <- repository.save( merged )
    } yield  merged
  }

  /**
   * Replaces an existing item by id
   *
   * @param id The id of the item to be replaced
   * @param replacement The value with which to replace
   * @return A future containing the updated value or an exception if item with given id could
   *         not be found
   */
  def replaceExisting( id:String, replacement:T)( implicit manifest: Manifest[T] ):Future[T] = {
    for {
      existing <- ~repository.findById( id )
      update <- Future { replacement }
      storeOperation <- repository.save( replacement )
    } yield update
  }

  /**
   * Deletes an existing item by id
   */
  def deleteExisting( id:String )( implicit manifest: Manifest[T] ):Future[T] = {
    for {
      domainObject <- ~repository.findById( id )
      _ <- repository.removeById( id )
    } yield domainObject
  }

}
