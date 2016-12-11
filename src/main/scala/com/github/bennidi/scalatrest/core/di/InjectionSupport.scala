package com.github.bennidi.scalatrest.core.di

import scaldi.{Injectable, Injector}

/**
 * Facade to avoid direct dependency to SCALDI and implicitly define the injector that is needed to resolve
 * injection requests.
 *
 * Modules
 * =======
 * Injectors are provided as module definitions (see http://scaldi.org/learn/#module) in a scala object.
 * The scala object enforces module definitions to be singletons such that components that extend the associated trait
 * do receive the same instance of that module.
 *
 * See {@link com.github.bennidi.scalatrest.core.di.SwaggerModule} as an example
 *
 */
trait InjectionSupport extends Injectable {

  // This implicit value can be provided with implicit imports.
  implicit val injector:Injector;

}
