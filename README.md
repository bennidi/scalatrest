# Scalatra Rest Template #

This template represents a blueprint for an integrated stack of technologies that can be used to build RESTful APIs
with as little boilerplate code as possible.
  
Like with Spring-Boot, **(integration-)tested** and documented REST resources can be built with low effort and few lines of code.
The stack is optimized for quick prototyping of REST resources around a domain model. Domain models are built as case classes. JSON/BSON transformations, CRUD respositories, CRUD integration tests are added as simple one liners or by extending predefined traits.

**Validation** of incoming JSON **with json schema** and endpoint access restriction with JSON Web-Tokens.

##Technologies

These are the core technologies used

  * [Scalatra](http://scalatra.org/) as the (servlet 3.0 based) core environment for running the application and defining rest resources
  * [Reactivemongo driver](https://github.com/ReactiveMongo/ReactiveMongo) and 
  [Reactivemongo-extensions](https://github.com/ReactiveMongo/ReactiveMongo-Extensions) 
  as the storage layer middleware. This implies usage
  of Mongodb as data storage
  * [Swagger](http://swagger.io/) integration adds convenient documentation support to any REST endpoint. For all registered
   REST services a Swagger 2.0 spec will be generated at runtime and exposed under a universal api-doc endpoint.
   This enables use of [Swagger-UI](http://petstore.swagger.io/) for each endpoint
  * [Scalatest](http://www.scalatest.org/) for fluent test writing using either BDD or simple flat spec like structuring of tests. Integration
   tests with [embeddedmongo](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo) and [WireMock](http://wiremock.org/) are included as well.
  * [Scaldi](http://scaldi.org/) is used to wire service dependencies. It's a very powerful DI framework with beautiful syntax
  * [Typesafe Config](https://github.com/typesafehub/config) is integrated with Scaldi to provide a convenient way of externalizing general and
  environment specific properties in HOCON or any other format supported by the config library.
  * Fullstack JSON: REST resources consume and produce JSON, the database stores BSON
  and the test fixtures are written in...JSON!
  + JSON WebToken support with additional integration security constraint checks configurable per route 
  

## Features

 * Reactively futuristic: Support for scala.concurrent.Future is built into persistence and REST layer by default.
 Being reactive with Futures and Akka across entire stack is easily possible
 * Easy CRUD: CRUD repository, CRUD REST resource (and related tests) are (quasi) one liners - it's done by composing traits
 * Convenient test infrastructure
   * BDD style assertions, Feature spec like test suite
   * Automatic handling of setup and teardown tasks
   * Json Fixtures for specifying any of the test data
   * Test coverage reports using `scoverage`
   * Integration test environment with embedded MongoDB, embedded Jetty   
   
# Development

## SBT

sbt can be started with a parameter to tell the application which config file to use. Without this parameter the default is application.conf (as described in [[Typesafe Config Guide]](https://github.com/typesafehub/config#standard-behavior)

For testing environment this is

    $ sbt -Dconfig.resource=application.testing.conf

## Test your setup ##

To compile the project and run the included test suite. This will download embeddedmongo and run unit as well as integration tests

    $ ;clean;test

To launch the app in a jetty container do

    $ container:start
    
Open [http://localhost:8080/api-docs](http://localhost:8080/api-docs) in a browser to verify that the container is up and running. 

> You need a mongodb running on the port as specified in `application.conf` or the config file you are using. Recommendation: Use `docker pull tutum/mongodb`

## Development ##

Activate automatic recompile and update of server:

    $ ~ ;copy-resources;aux-compile

Run all tests

    $ test 

Run unit tests only

    $ test-only com.github.bennidi.scalatrest.unit.* 
   
Run integration tests only

    $ test-only com.github.bennidi.scalatrest.integration.*    

Run tests with coverage instrumentation. Reports are found in `/target/scala-xxx/coverage-reports` folder

    $ ;coverage;test;coverageReport 
    
Generate documentation (ScalaDoc). Generated site will be located in `/target/scala-xxx/[api|test-api]` folder
    
    $ doc
    $ test:doc

OPTIONAL: The stack uses Swagger 1.0 for documentation of APIs. To make use of the Swagger features, you can use Swagger UI. Either you use it online or you maintain a local copy (recommended). Clone Swagger-UI into a directory different from your repository.

    $ git clone https://github.com/swagger-api/swagger-ui.git
    
You can then open `swagger-ui/dist/index.html` in your preferred browser.    
   
   
## Handling CSV based imports

There is no direct support for csv imports anywhere in the template code. The data format supported by
the template is **JSON**. To import a .csv file, it has to be transformed into a valid JSON. This can
easily be done with a simple node.js tool, called [csvtojson](https://www.npmjs.com/package/csvtojson).

The converter tool expects column headers to contain the mapping from the flat CSV line to a (possibly)
nested JSON object. Each column header can be a path expression pointing to the target property of where to
store the value of the column.

  * Install the tool using npm
  * Add desired column headers to your CSV
  * Feed the csv into the tool and wait for the JSON to appear on the other side
   
#License

This template constitutes an exemplary integration of technologies that can be used as a basis for building
REST resources. It DOES NOT redistribute source code or binaries of any of its upstream dependencies as it
not a bundled artifact.

SUGGESTED LICENSE is APACHE 2

## Project dependencies

|Framework|Version|sbt-coordinates|License|
|:----:|:----:|:----:|:----:|
|Scalatra|||BSD|
|Reactivemongo + extensions|||Apache 2|
|flapdoodle-oss embed mongo|||No license given|
|Scaldi|||Apache 2|
|Swagger|||Apache 2|
|json4s|||Apache 2|
|wiremock|||Apache 2|
|scalaj-http|||Apache 2|
|Typesafe config|||Apache 2|
|Jetty|||Apache 2|
|logback|||EPL v1.0 and LGPL 2.1|
