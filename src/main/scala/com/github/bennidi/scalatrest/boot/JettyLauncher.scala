package com.github.bennidi.scalatrest.boot

/**
 * @author ngocht 
 * @date 21.07.15.
 * @copyright n10t.org
 */

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyLauncher {

  // this is my entry object as specified in sbt project definition
  def main( args: Array[String] ) {
    val port = if ( System.getenv( "PORT" ) != null ) System.getenv( "PORT" ).toInt else 8080

    val server = new Server( port )
    val context = new WebAppContext( )
    context setContextPath "/"
    context.setResourceBase( "src/main/webapp" )
    context.setInitParameter(ScalatraListener.LifeCycleKey, "com.github.bennidi.scalatrest.boot.Application")
    context.addEventListener( new ScalatraListener )
    context.addServlet( classOf[DefaultServlet], "/" )

    server.setHandler( context )

    server.start
    server.join
  }
}