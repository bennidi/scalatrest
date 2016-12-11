package com.github.bennidi.scalatrest.model.base

import java.util.UUID

/**
 * Any class of objects that can be uniquely identified with a UUID must implement this trait.
 * This trait indicates that the class has an identity and lifecycle of its own. UUIDentifiable
 * objects are not embedded in other UUIDentifiable but referenced with an EmbeddableView.
 *
 * Each class of UUIDentifiable maps to its own collection|table
 *
 */
trait UUIDentifiable {

  def _id:String

  def isSameId[T <:UUIDentifiable](x:T): Boolean ={
    return this._id.equals( x._id )
  }

  def id() = {
    _id
  }

}
object UUIDentifiable{

  def generateId(): String = UUID.randomUUID().toString

}


/**
 * An Embeddable view is a projection of a UUIDentifiable object that is embedded in an enclosing
 * object. It stores the UUID of the referenced object and a set of properties commonly accessed with
 * this
 */
trait EmbeddableView {

  def ref:String

}




