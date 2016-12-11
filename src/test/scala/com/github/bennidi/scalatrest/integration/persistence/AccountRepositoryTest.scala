package com.github.bennidi.scalatrest.integration.persistence

import com.github.bennidi.scalatrest.api.model.{Account, AccountRepository}
import com.github.bennidi.scalatrest.base._
import com.github.bennidi.scalatrest.base.{PersistenceCRUD, PersistenceSpec, TestFixtureProvider}
import com.github.bennidi.scalatrest.persistence.BaseRepository
import org.json4s.JsonAST.JObject
import org.scalatest.DoNotDiscover
import reactivemongo.extensions.dao.BsonDao

@DoNotDiscover
class AccountRepositoryTest extends PersistenceCRUD[Account]{

  override val repository: BaseRepository[Account] = inject[AccountRepository]
}
