package com

/**
 * == Documentation Overview ==
 *
 *  Here a list classes to check out for a high-level view of things:
 *
 *    - [[com.github.bennidi.scalatrest.boot.Application]] : Loaded at startup => the place to mount Rest
 *    - [[com.github.bennidi.scalatrest.api.core.CrudResource]] : Base class for getting full CRUD support in any REST resource
 *    - [[com.github.bennidi.scalatrest.persistence.BaseRepository]] : MongoDB persistence for models
 *    - [[com.github.bennidi.scalatrest.core.di.DIModules]] : Dependency injection support and aggregation of modules.
 *      NOTE: Use [[com.github.bennidi.scalatrest.core.di.ProjectDependenciesModule]] to define your custom dependencies
 *    - [[com.github.bennidi.scalatrest.api.core.auth.JwtAuthFilter]] : JSON Web Token based access authorization
 *    - [[com.github.bennidi.scalatrest.api.core.HttpCallSupport]] : Support for inter service calls
 *    - [[com.github.bennidi.scalatrest.api.core.crud.SearchEndpointSupport]] : Adds a generic /search endpoint that consumes
 *    MongoDB style queries and [[com.github.bennidi.scalatrest.persistence.Alias]]
 *    - [[com.github.bennidi.scalatrest.api.core.ApiDocumentationSupport]] : Integration with Swagger to expose Api documentation
 *    as a rest resource
 *
 *
 *  HINTS:
 *  + Many classes have a corresponding companion object.You can switch between "class view" and
 *  "companion object view" by clicking on the icon in the upper left corner.
 *
 * There is a tutorial explaining the creation of a Rest resource that touches most of the
 * features provided by the template code:
 *
 *    - [[com.github.bennidi.scalatrest.model Creating a model]]
 *    - [[com.github.bennidi.scalatrest.persistence Adding persistence]]
 *    - [[com.github.bennidi.scalatrest.api Composing a REST resource]]
 *    - [[com.github.bennidi.scalatrest.core.di Configuring Dependency Injection]]
 *    - The guide for writing tests is part of the Scaladoc of the test sources.
 *      Run test:doc to generate that documentation and navigate to package com.github.bennidi.scalatrest.integration
 *
 *  Also: Check out [[com.github.bennidi.scalatrest.api.resources.AccountResource]] for a reference implementation

 *
 */
package object okotta {

}