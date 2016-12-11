package com.github.bennidi.scalatrest.api.core

import org.scalatra.{ScalatraBase, ScalatraServlet}
import org.slf4j.LoggerFactory

/**
 * Base class for all servlets.
 */
trait AbstractServletBase extends ScalatraServlet with LogSupport

/**
 * Add logger to class
 */
trait LogSupport{

  val log =  LoggerFactory.getLogger(getClass)

  def logError(msg:String, throwable: Throwable):Unit = {
    log.info(msg)
    if(log.isDebugEnabled()) throwable.printStackTrace()
  }

}

