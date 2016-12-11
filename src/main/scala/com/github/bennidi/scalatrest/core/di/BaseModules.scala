package com.github.bennidi.scalatrest.core.di

import java.io.InputStream
import java.security.{PublicKey, PrivateKey, KeyStore, KeyPair}

import com.github.bennidi.scalatrest.api.core.RestResourceInfo
import com.github.bennidi.scalatrest.api.core.auth.{JwtManager, AccessControlList}
import com.github.bennidi.scalatrest.api.resources.AccountResource
import com.github.bennidi.scalatrest.api.model.AccountRepository
import com.github.bennidi.scalatrest.persistence.core.GlobalExecutionContext
import reactivemongo.api.{DB, MongoDriver}
import scaldi.{Injector, TypesafeConfigInjector, Module}

/**
 * Export dependencies required for persistence.
 */
trait PersistenceModuleSupport extends InjectionSupport{

  override implicit val injector:Injector = MongoDBModule.Dependencies

}

/**
 * Create bindings for connection to MongoDB. Creates a driver with default
 * actor system.
 */
class MongoDBModule extends Module with GlobalExecutionContext{

  bind[MongoDriver] to new MongoDriver() destroyWith (_.close())

  bind[DB] to {
    val driver = inject [MongoDriver]
    val connection = driver.connection(List(inject [String] ("services.storage.mongodb.connection")))
    val db = connection(inject [String] ("services.storage.mongodb.dbname"))
    db
  }

}

/**
 * Load and bind keystore as well as the component that provides token
 * encode/decode functionality [[com.github.bennidi.scalatrest.api.core.auth.JwtManager]]
 */
class AuthorizationModule extends Module{

  bind[KeyPair] to {
    val jwtAliasName = inject[String] ("security.auth.jwtAliasName")
    val keystorePassword = inject[String] ("security.auth.keystorePassword")
    val jwtAliasPassword =inject[String] ("security.auth.jwtAliasPassword")
    val jwtKeystore = inject[String] ("security.auth.jwtKeystore")

    val keystore = KeyStore.getInstance(KeyStore.getDefaultType)
    val asStream: InputStream = getClass.getClassLoader.getResourceAsStream(jwtKeystore)
    keystore.load(asStream, keystorePassword.toCharArray)
    new KeyPair(keystore.getCertificate(jwtAliasName).getPublicKey,
      keystore.getKey(jwtAliasName, jwtAliasPassword.toCharArray).asInstanceOf[PrivateKey])
  }

  bind[PublicKey] to { val keys = inject[KeyPair];keys.getPublic }
  bind[PrivateKey] to { val keys = inject[KeyPair];keys.getPrivate }
  bind[JwtManager] to new JwtManager

}

object MongoDBModule{

  val Dependencies = TypesafeConfigInjector() :: new MongoDBModule :: new AuthorizationModule

}

/**
 * Sample module for status resource
 */
class AccountModule extends Module with GlobalExecutionContext{

  bind[AccountRepository] to new AccountRepository
  bind[AccountResource] to new AccountResource

  bind[AccessControlList] identifiedBy 'accounts to {
    val aclDef = inject[String] ("security.auth.acl.accounts")
    new AccessControlList(aclDef)
  }

  bind[RestResourceInfo] identifiedBy 'accounts to RestResourceInfo(
    urlBase = "/accounts",
    singular = "account",
    plural = "accounts",
    description = "User Accounts Endpoint" )

}