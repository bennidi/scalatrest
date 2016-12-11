package com.github.bennidi.scalatrest.core.di

import scaldi.{Injector, Module}

trait ProjectDependenciesModule extends InjectionSupport {

  override implicit val injector:Injector = new ProjectDependencies

}

/**
 * This is the place where individual project dependencies go.
 * They are integrated with the AllDependenciesModule
 */
class ProjectDependencies extends Module{


}