import sbt.{ModuleID, Resolver}

object ProjectConfig{

  val Name = "ScalatraREST"
  val Version = "0.1.0-SNAPSHOT"
  val ID = "ScalatraREST"

  // Add project specific dependencies here
  val dependencies:Seq[ModuleID] = List.empty

  // Add project specific resolvers here
  val resolvers:Seq[Resolver] = List.empty
  
}