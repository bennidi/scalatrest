package com.github.bennidi.scalatrest.api.resources

import com.github.bennidi.scalatrest.api.core.auth.{AccessControlList, AclProvider, JwtAuthFilter}
import com.github.bennidi.scalatrest.api.core.crud.SearchEndpointSupport
import com.github.bennidi.scalatrest.api.core.{JsonSchemaProvider, CrudResource, RestResourceInfo}
import com.github.bennidi.scalatrest.api.model.{Account, AccountRepository}

/**
 * This resource exists mostly for illustrative purposes and as a subject for integration testing.
 * It is not mounted in the Application and will not appear anywhere in the live system.
 * It can be deployed, however, in an embedded Container to serve as an endpoint for testing of the
 * various features provided by this stack.
 *
 * @param manifest The manifest is needed by third-party libs (for runtime inspections mostly)
 */
class AccountResource(implicit val manifest: Manifest[Account] ) // manifest needed by third-party libs
  extends CrudResource[Account] // Add full set of CRUD operations
          with JwtAuthFilter // Require a valid JSON Web Token for access of any route
          with AclProvider // Implementing JwtAuthFilter requires an ACL to be in scope
          with SearchEndpointSupport[Account] // Add the generic search endpoint
          with JsonSchemaProvider[Account] { // Add endpoint for related schema

  // The repository that can handle Account objects (required by RepositorySupport)
  val repository = inject[AccountRepository]
  // An access control list is maintained per REST resource
  val accessControl = inject[AccessControlList]("accounts")
  // The ResourceInfo groups some reusable information about this resource
  // NOTE the lazy val that is necessary in this case because of the way this value is accessed
  lazy val resourceInfo: RestResourceInfo = inject[RestResourceInfo]("accounts")
}

