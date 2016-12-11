#Design & Architecture

## Functional Domain Driven Design

DDD like approach to domain modeling (rich domain model with some form of roots, access hierarchy etc.)?
    * http://martinfowler.com/bliki/AggregateOrientedDatabase.html
    * https://www.arangodb.com/presentations/NewYorkJava.pdf

A purely object oriented implementation of DDD seems undesirable. However, there exists the approach
of implementing DDD using functional techniques: 
  * Define the domain model structure as ADTs (case class + pattern matching). Focus entirely on structure
  leaving out the behaviour and any state transitions
  * Encapsulate behaviour in pure, composable functions
  * Handle state changes in a functional way: non-destructive -> always create new domain objects
  * Capture application state changes as a sequence of persistent domain events

Check out these resources for in-depth explanations
  * [Debasish Gosh - Functional Domain Models](https://www.youtube.com/watch?v=95KztoeGHl0)
  * [Debasish Gosh - Functional Domain Models](http://debasishg.blogspot.se/2014/05/functional-patterns-in-domain-modeling.html)
  * [Debasish Gosh - Functional and Reactive Domain Modeling](http://www.manning.com/ghosh2/)
  * [Debasish Gosh - Functional DDD](http://www.slideshare.net/debasishg/functional-patterns-in-domain-modeling)
  * [](https://skillsmatter.com/skillscasts/3191-ddd-functional-programming)
  * [](https://skillsmatter.com/skillscasts/2197-talk-by-patrick-fredriksson)
  
  
#Resources

##REST
 * http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api
 * http://blog.mwaysolutions.com/2014/06/05/10-best-practices-for-better-restful-api/
 * http://www.slideshare.net/mario_cardinal/best-practices-for-designing-pragmatic-restful-api

 
###Guidelines
 * [HTTP PATCH](https://www.mnot.net/blog/2012/09/05/patch)
 * [Scalable Web APIs](http://www.bizcoder.com/index.php/2011/04/11/web-apis-dont-be-a-victim-of-your-success/)
 * [Building Media Types](http://www.amundsen.com/blog/archives/1097)
 * [Evolvable Systems: A perspective on REST](http://amundsen.com/blog/archives/1099)
 * [Hypermedia Types](http://www.amundsen.com/hypermedia/)
 
###Examples
 * https://developer.github.com/v3/
 * https://github.com/WhiteHouse/api-standards
  
###Swagger
  * https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md

###HATEOAS
  * https://thisweekinrest.wordpress.com/tag/hateoas/page/2/
  * [Example of HATEOAS with Scalatra](https://gist.github.com/rhinoman/5880814)
  * [Overview of HAL Spec](http://stateless.co/hal_specification.html)


###JSON Schema
  * http://schemastore.org/json/

##Scaldi
  * [Injector already frozen](https://groups.google.com/forum/#!topic/scaldi/iilfOLeSPQM)
  * [Sample Scaldi Module](https://gist.github.com/ctcarrier/11348157)

##Scala

### General
 * [Twitter - Effective Scala](http://twitter.github.io/effectivescala/)


### Concurrency

 * http://programmers.stackexchange.com/questions/131636/performance-of-scala-compared-to-java
 * http://docs.scala-lang.org/overviews/core/futures.html
 * [Scala Enums](https://github.com/scalatra/scalatra/issues/343)
 * [Futures](http://danielwestheide.com/blog/2013/01/09/the-neophytes-guide-to-scala-part-8-welcome-to-the-future.html)
 * [Promises](http://danielwestheide.com/blog/2013/01/16/the-neophytes-guide-to-scala-part-9-promises-and-futures-in-practice.html)

### Type System
 * [The Option type](http://danielwestheide.com/blog/2012/12/19/the-neophytes-guide-to-scala-part-5-the-option-type.html)
 * [Traits vs. abstract class](http://stackoverflow.com/questions/1229743/what-are-the-pros-of-using-traits-over-abstract-classes)
 * [Manifest](http://stackoverflow.com/questions/3213510/what-is-a-manifest-in-scala-and-when-do-you-need-it)
 * [Implicit Type Classes replace adapter pattern](http://debasishg.blogspot.de/2010/06/scala-implicits-type-classes-here-i.html)
 * [Type Tags](http://docs.scala-lang.org/overviews/reflection/typetags-manifests.html)



##JSON & BSON
 * [Generic Conversion Backend](https://github.com/netvl/picopickle)
 * [Polymorphism with json4s](https://gist.github.com/worthlesscog/3060273)
 * [Json<->ScalaCollections](https://github.com/nestorpersist/json)
 * [Bson<->Json Bridge](https://github.com/Jacoby6000/Json4s-reactivemongo-compatibility)
 * https://github.com/json4s/json4s
 * https://github.com/michel-kraemer/bson4jackson
 * http://stackoverflow.com/questions/24442618/serialize-and-deserialize-scala-enumerations-or-case-objects-using-json4s
 * http://stackoverflow.com/questions/17135166/looking-for-a-good-example-of-polymorphic-serialization-deserialization-using-ja

##Scalatra
 * [Error handling](https://groups.google.com/forum/#!topic/scalatra-user/8PHwzJybrOw)
 * http://www.slideshare.net/lobster1234/oscon-2014-api-ecosystem-with-scala-scalatra-and-swagger-at-netflix
 * http://stackoverflow.com/questions/17215330/scalatra-commands-and-validating-nested-objects
 * https://github.com/scalatra/scalatra-in-action/blob/master/chapter09-standalone/src/main/scala/ScalatraLauncher.scala
 
##Example Code
  * https://github.com/ttiurani/extendedmind
  
  
+ SchemaValidator
+ ManifestProvider
+ Alias
  