package com.github.bennidi.scalatrest.core.di

import com.github.bennidi.scalatrest.core.di.DIModules.AggregatedDependencies
import scaldi.{Injector, TypesafeConfigInjector}

/**
 * Mixing in this trait will give access to dependency injection functionality
 */
trait DIModules extends InjectionSupport {

  override implicit val injector:Injector = AggregatedDependencies

  def shutdownModules() = {
    DIModules.PersistenceModule.destroy()
  }
}

object DIModules {

  val SwaggerModule = new SwaggerModule
  val PersistenceModule = new MongoDBModule
  val ProjectModule = new ProjectDependencies
  val AuthorizationModule = new AuthorizationModule
  val ResourceStatusInjections = new AccountModule

  val AggregatedDependencies:Injector =
    TypesafeConfigInjector()  :: //https://github.com/typesafehub/config#standard-behavior
      PersistenceModule ::
      AuthorizationModule ::
      SwaggerModule ::
      ResourceStatusInjections ::
      ProjectModule

}

