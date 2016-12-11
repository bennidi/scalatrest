package com.github.bennidi.scalatrest.persistence.core

import scala.concurrent.ExecutionContext

/**
 * Implicitly expose an execution context (needed by AkkA system) by explicitly
 * extending a trait instead of using the implicit imports (that can easily be overlooked)
 *
 * Exposing an execution context via this trait also allows to control the type of
 * execution context via dependency injection or other means.
 *
 */
trait GlobalExecutionContext {
  // reexpose the globally available execution context
  implicit val ec:ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit def executor():ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}
