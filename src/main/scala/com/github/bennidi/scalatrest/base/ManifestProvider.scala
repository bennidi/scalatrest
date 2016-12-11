package com.github.bennidi.scalatrest.model.base

/**
 * Parts of the system and its libraries, such as
 *   - [[org.scalatra.swagger.SwaggerSupport]],
 *   - [[com.github.bennidi.scalatrest.api.core.JsonSchemaProvider]],
 *   - [[com.github.bennidi.scalatrest.base.TestFixtureProvider]]
 * rely on the availability of a [[Manifest]] to describe a certain type. A manifest is part of
 * Scala's reflective system and provides access to information usually unavailable like actual
 * types of generic type parameters.
 *
 * ManifestProvider is a way for any component that implicitly requires a Manifest to be available
 * to state that dependency explicitly. This way compiler will error when a Manifest is not provided but not
 * where the Manifest is required but where it is not provided.
 */
trait ManifestProvider[T <: UUIDentifiable] {

  implicit def manifest: Manifest[T]

  def getSimpleClassName():String = {
    val name = manifest.runtimeClass.getSimpleName
    // Workaround: manifest construction by hand and by compiler seem to differ
    if(name.endsWith("$")) name.substring(0, name.length -1) else name
  }
}