package com.github.bennidi.scalatrest.api.model

import com.github.bennidi.scalatrest.api.core.JObjectToBson
import com.github.bennidi.scalatrest.model.base.UUIDentifiable
import com.github.bennidi.scalatrest.persistence.BaseRepository
import org.json4s.JsonAST.JObject
import reactivemongo.bson.Macros
import scala.concurrent.ExecutionContext

/**
 * Simple model of user accounts.
 *
 * @param _id Any persistent model has a unique id (UUID) that is controlled be the application
 * @param name
 * @param code
 * @param details
 */
case class Account(_id: String = UUIDentifiable.generateId( ),
                   name: String, code: Int,
                   details: AccountDetails = AccountDetails( ) ) extends UUIDentifiable

/**
 * The nested case class to containing details about the account. The details are passed as a generic
 * JObject in order to allow any structure to be accommodated. This is also done for illustrative purpose
 * and to have a base class for testing JObject<->BSON conversions
 *
 * @param details A generic container for additional information about the account
 */
case class AccountDetails(details: JObject = JObject( ) )

/**
 * The companion object of a persistent model class is the preferred location
 * for the implicit Readers and Writers that are needed by the reactivemongo driver to do its work.
 */
object Account extends JObjectToBson{

  // The BSON handler for the nested case class (AccountDetails) needs to be declared before the handler
  // of the class (Account) that references this handlers target class
  // In other words: Assuming a tree constructed from (nested) case classes
  // the order of definition of handlers is from leaves to root
  implicit val ResourceStatusDetailsConversion = Macros.handler[AccountDetails]
  implicit val ResourceStatusConversion = Macros.handler[Account]

}

/**
 * An advanced CRUD repository is available by means of extension of a base class. The repository can then be extended
 * to add more model specific persistence.
 *
 * @param ec The execution context is required by the BaseRepository. It is resolved wherever an
 *           instance of this repository is created. The compiler will complain if there is no implicit
 *           in scope. With the current setup the place of resolution is the Scaldi Module that does the setup
 *           of bindings (mostly auto-wiring by type)
 */
class AccountRepository(implicit ec: ExecutionContext ) extends BaseRepository[Account]( "accounts" )