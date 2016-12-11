package com.github.bennidi.scalatrest.base

import javax.servlet.ServletContext

import com.github.bennidi.scalatrest.api.resources.AccountResource
import com.github.bennidi.scalatrest.boot.ScalatrestBootstrap
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener


trait JettyContainerSupport{

  self: SuiteTaskSupport =>

  private[this] var server:Server = null
  def bootstrapClass:String = "com.github.bennidi.scalatrest.base.StatusResource"

  addSetupTask( ( ) => {
    log.info("######### STARTUP Jetty container ##########")
    server = new Server( 8088 )
    val context = new WebAppContext( )
    context setContextPath "/"
    context.setResourceBase( "src/test/resources" )
    context.setInitParameter(ScalatraListener.LifeCycleKey, bootstrapClass)
    context.addEventListener( new ScalatraListener )
    context.addServlet( classOf[DefaultServlet], "/" )
    server.setHandler( context )
    server.start
  } )

  // Cleanup collection
  addTeardownTask( ( ) => {
    log.info("######### SHUTDOWN Jetty container ##########")
    server.stop()
  } )


}

/**
 * This class is meant to be customized by projects that use this template.
 * Primary function is mounting of REST endpoints
 */
class StatusResource extends ScalatrestBootstrap{

  lazy val resourceStatusResource = inject[AccountResource]
  override def mountResources( context: ServletContext ): Unit = {
    context.mount(resourceStatusResource, "/status-resource/*" )
  }
}