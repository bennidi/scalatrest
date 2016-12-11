package com.github.bennidi.scalatrest.persistence

import com.github.bennidi.scalatrest.api.core.{JObjectToBson, Json4sToBson}
import com.github.bennidi.scalatrest.core.di.{PersistenceModuleSupport}
import com.github.bennidi.scalatrest.persistence.core.GlobalExecutionContext
import com.github.bennidi.scalatrest.utils.StringUtils
import org.json4s.JsonAST.JObject
import reactivemongo.api.DB
import reactivemongo.bson.{BSONDocumentWriter, BSONDocumentReader}
import reactivemongo.extensions.dao.BsonDao
import reactivemongo.extensions.dsl.BsonDsl

import scala.concurrent.ExecutionContext
import Json4sToBson._

object MongoContext extends PersistenceModuleSupport {

  lazy val db: DB = inject[DB]
}

/**
 * Adds finder method for named models (models that have an attribute "name").
 * Can be mixed into [[BaseRepository]]
 */
trait FindByNameSupport[MODEL] extends BsonDsl with StringUtils{
  self:BsonDao[MODEL,String] with GlobalExecutionContext=>


  def byName( name: String ) = {
    val useRegex = containsRegexCharacters( name )
    if ( name.isEmpty ) findAll( )
    else findAll( if ( useRegex ) "name" $regex(name, "i") else "name" $eq name )
  }
}

/**
 * Provides extended CRUD to implementing subclasses. It's a thin wrapper around [[BsonDao]]
 * to alleviate subclasses from some technical details like how to provide injection of MongoDB context.
 *
 * @param collection - The name of the collection in MongoDB
 * @param ec - The execution context used to run the async IO operations
 * @param reader - The reader that transforms BSON to model class [[reactivemongo.bson.Macros]]
 * @param writer - The writer that transforms model class to BSON [[reactivemongo.bson.Macros]]
 * @tparam MODEL - The type of model handled by this repository
 */
abstract class BaseRepository[MODEL](collection:String)(implicit ec:ExecutionContext,
                                                        implicit val reader: BSONDocumentReader[MODEL],
                                                        implicit val writer: BSONDocumentWriter[MODEL])
  extends BsonDao[MODEL, String]( MongoContext.db, collection )
          with PersistenceModuleSupport with GlobalExecutionContext

/**
 * Handles JObject directly (no model classes)
 */
class GenericJObjectRepository(collection:String)(implicit ex:ExecutionContext) extends BaseRepository[JObject](collection)