package com.github.bennidi.scalatrest.persistence

import com.github.bennidi.scalatrest.api.core.JObjectToBson
import com.github.bennidi.scalatrest.model.base.UUIDentifiable
import com.github.bennidi.scalatrest.persistence.core.GlobalExecutionContext
import org.json4s.JsonAST.JObject
import reactivemongo.extensions.dao.BsonDao
import reactivemongo.extensions.dsl.BsonDsl

/**
 *
 * Aggregation of necessary functionality for connection to storage using a repository
 * and auto-transformation into Json space
 *
 * @tparam T
 */
trait RepositorySupport[T <: UUIDentifiable] extends BsonDsl
                                                     with JObjectToBson
                                                     with GlobalExecutionContext {

  val repository: BaseRepository[T]

}

/**
 *
 * Aggregation of necessary functionality for connection to storage using a repository
 * and auto-transformation into Json space
 *
 * @tparam T
 */
trait JsonRepositorySupport[T <: UUIDentifiable] extends BsonDsl
                                                     with JObjectToBson
                                                     with GlobalExecutionContext {

  val repository: BaseRepository[JObject]

}


