package com.github.bennidi.scalatrest.core.di

import com.github.bennidi.scalatrest.api.core.SwaggerApiDocProvider
import org.scalatra.swagger.{ApiInfo, Swagger}
import scaldi.Module


class SwaggerModule extends Module {

    bind [SwaggerApiDocProvider] to new SwaggerApiDocProvider

    bind[Swagger] to new Swagger(Swagger.SpecVersion, "1", ApiInfo(
      title = "Scalatrest REST Api",
      description = "Scalatrest Api",
      termsOfServiceUrl = "Scalatrest Api",
      contact = "bennidi on github",
      license = "MIT",
      licenseUrl = "Scalatrest Api"
    ))

}
