package com.github.bennidi.scalatrest.boot

import javax.servlet.ServletContext

import com.github.bennidi.scalatrest.api.resources.AccountResource

/**
 * This class is meant to be customized by projects that use this template.
 * Primary function is mounting of REST endpoints
 */
class Application extends ScalatrestBootstrap{

  lazy val accountResource = inject[AccountResource]
  override def mountResources( context: ServletContext ): Unit = {
    context.mount(accountResource, s"${accountResource.resourceInfo.urlBase}/*" )
  }
}
