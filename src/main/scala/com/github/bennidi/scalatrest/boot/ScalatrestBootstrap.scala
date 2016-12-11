package com.github.bennidi.scalatrest.boot

import javax.servlet.ServletContext

import com.github.bennidi.scalatrest.api.core.SwaggerApiDocProvider
import com.github.bennidi.scalatrest.core.di.{DIModules}
import org.scalatra._
import org.slf4j.LoggerFactory


/**
 * Bootstrapping class for Scalatra (see servlet context
 * parameter org.scalatra.LifeCycle in web.xml)
 *
 * Adds Swagger api-docs
 *
 *
 * Resources
 * =========
 * [[http://scalatra.org/2.4/guides/deployment/configuration.html Scalatra configuration guide]]
 */
abstract class ScalatrestBootstrap extends LifeCycle with DIModules {

  val logger = LoggerFactory.getLogger(getClass)

  val swaggerApiDocProvider:SwaggerApiDocProvider = inject [SwaggerApiDocProvider]

  override def init(context: ServletContext) {
    context.mount(swaggerApiDocProvider, "/api-docs") // the endpoint for swagger documentation
    mountResources(context)
  }


  override def destroy(context: ServletContext) {
    super.destroy(context)
    shutdownModules()
  }

  /**
   * This is a hook provided for dependent projects to mount their resources in the Application subclass
 *
   * @param context
   */
  def mountResources(context:ServletContext)
}

