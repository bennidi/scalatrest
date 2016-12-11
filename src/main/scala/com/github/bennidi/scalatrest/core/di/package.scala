package com.github.bennidi.scalatrest.core

/**
 * = Dependency Injection with Scaldi =
 *
 * > Wiring up the object model with smart and type safe Scaldi modules
 *
 * [[http://scaldi.org/learn/ Scaldi]] has been integrated into the stack to provide a very readable
 * and powerful way for wiring the object models.
 *
 * [[scaldi.Module]] is used to define bindings of concrete instances to type tags. Type tags
 * can then by used to inject a bound value in client code.
 *
 *
 * {{{
 *
 *   implicit val injector = new Module {
 *     bind [DB] to new NormalDb
 *     bind [DB] identifiedBy required('experimental) to new ExperimentalDb
 *   }
 *
 *   val db = inject [DB] // injected db will be NormalDb
 *   val db = inject [DB]("experimental") // injected db will be ExperimentalDb
 *
 *
 *   // more complex bindings can be defined using block expressions
 *   bind[KeyPair] to {
 *    val jwtAliasName = inject[String] ("security.auth.jwtAliasName")
 *    val keystorePassword = inject[String] ("security.auth.keystorePassword")
 *    val jwtAliasPassword =inject[String] ("security.auth.jwtAliasPassword")
 *    val jwtKeystore = inject[String] ("security.auth.jwtKeystore")
 *
 *    val keystore = KeyStore.getInstance(KeyStore.getDefaultType)
 *    val asStream: InputStream = getClass.getClassLoader.getResourceAsStream(jwtKeystore)
 *    keystore.load(asStream, keystorePassword.toCharArray)
 *    new KeyPair(keystore.getCertificate(jwtAliasName).getPublicKey,
 *      keystore.getKey(jwtAliasName, jwtAliasPassword.toCharArray).asInstanceOf[PrivateKey])
 *   }
 *
 * }}}
 *
 *  == Composing modules ==
 *
 *  Scaldi modules can be composed into aggregated modules (which are but modules themselves).
 *  Resolution of dependencies is bottom up (when requested value is not found it is searched
 *  in module one level upwards until root is reached).
 *
 *  Module composition is used to separate core modules (defined in this template) from user modules
 *  (defined by each repository based on this template). [[com.github.bennidi.scalatrest.core.di.DIModules]] aggregates
 *  all modules and re-exposes them using an implicit [[scaldi.Injector]].
 *
 *  The injector is the magical component that resolves requested dependencies. It needs to be in place wherever
 *  a dependency is requested. This is guaranteed by the [[com.github.bennidi.scalatrest.core.di.DIModules]] trait
 *  which not only exposes the injector but also provided the ability to use the [[scaldi.Injectable.inject]]
 *  macro.
 *
 *  For the DataModel use case the code would look like this:
 *
 *  {{{
 *
 *  class DataModelModule extends Module with GlobalExecutionContext{
 *
 *    bind[DataModelRepository] to new DataModelRepository
 *    bind[DataModelResource] to new DataModelResource
 *
 *    bind[AccessControlList] identifiedBy 'datamodel to {
 *      val aclDef = inject[String] ("security.auth.acl.datamodel")
 *      new AccessControlList(aclDef)
 *    }
 *
 *  bind[RestResourceInfo] identifiedBy 'datamodel to RestResourceInfo(
 *    urlBase = "/model",
 *    singular = "data model instance",
 *    plural = "data model instances",
 *    description = "Data Model Endpoint" )
 *
 *  }
 *
 *  }}}
 *
 *  '''IMPORTANT'''
 *
 *  All user defined modules need to be composed in [[com.github.bennidi.scalatrest.core.di.ProjectDependenciesModule]]
 *  such that they will be aggregated into the global module that is available to all clients of
 *  [[com.github.bennidi.scalatrest.core.di.DIModules]] (by default: any rest resource, repository and integration test)
 *
 *
 *  == Configuration with properties==
 *
 *  Configuration of properties is supported using [[scaldi.TypesafeConfigInjector]] which supports
 *  loading of configuration files.
 *
 *  Config files are written in [[https://github.com/typesafehub/config/blob/master/HOCON.md HOCON]].
 *  Read [[https://github.com/typesafehub/config this guide]] for reference
 *  of how config files are written, composed and discovered.
 *
 *  [[com.github.bennidi.scalatrest.api Previous: The REST resource]]  - [[com.github.bennidi.scalatrest.integration Next: Integration Testing]]
 *
 */
package object di {

}
