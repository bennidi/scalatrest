import sbt._
import sbt.Keys._
import org.scalatra.sbt._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._
import sbtassembly.AssemblyPlugin._
import sbtassembly.AssemblyKeys._
import scoverage.ScoverageKeys.coverageExcludedPackages

object ScalatrestApiBuild extends Build {

  val Organization = "com.github.bennidi.scalatrest"
  val Name = ProjectConfig.Name
  val Version = ProjectConfig.Version
  val ScalaVersion = "2.11.6"
  val ScalatraVersion = "2.4.0-RC2-2"

  val myAssemblySettings = assemblySettings ++ Seq(
    mainClass in assembly := Some( "com.github.bennidi.scalatrest.boot.JettyLauncher" ),
    test in assembly := {},
    parallelExecution := false,
    assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
  )

  lazy val project = Project(
    ProjectConfig.ID,
    file( "." ),
    settings = ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ myAssemblySettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      coverageExcludedPackages :=
        // Do not test application bootstrap
        "com.github.bennidi.scalatrest.boot.*;com.github.bennidi.scalatrest.model.base.*" +
          ";com.github.bennidi.scalatrest.api.error.*" +
          // Jade template rendering is currently not used
          ";.*jade;.*TemplateRenderingSupport" +
          ";.*Swagger.*;.*LogSupport;.*AbstractServletBase" +
          // Unused postprocessors
          ";com.github.bennidi.scalatrest.api.core.postprocessing.*" +
          ";.*Json4sToBson;.*MapBSON;.*MapValue" +
          // SCALDI modules can not be tested, really
          ";com.github.bennidi.scalatrest.core.di.*;com.github.bennidi.scalatrest.utils.*",
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
      resolvers += "Reactive Couchbase Releases" at "https://raw.github.com/ReactiveCouchbase/repository/master/releases/",
      resolvers ++= ProjectConfig.resolvers,
      libraryDependencies ++= Seq(
        // Scalatra core dependencies
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalatest" % "2.4.0.RC1" % "test",
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-swagger" % ScalatraVersion,
        // Data storage layer
        "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
        "org.reactivemongo" %% "reactivemongo-extensions-bson" % "0.10.5.0.0.akka23",
        "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.48.0",
        "org.scaldi" %% "scaldi" % "0.5.6",
        // REST layer
        "io.swagger" % "swagger-core" % "1.5.0",
        "io.swagger" %% "swagger-scala-module" % "1.0.0",
        "org.json4s" %% "json4s-jackson" % "3.2.11",
        "com.github.fge" % "json-schema-validator" % "2.2.6",
        "org.scalaj" %% "scalaj-http" % "1.1.5",
        "com.github.tomakehurst" % "wiremock" % "1.57" % "test",
        "com.pauldijou" %% "jwt-core" % "0.4.1",
        // Utils
        "com.typesafe" % "config" % "1.3.0",
        "ch.qos.logback" % "logback-classic" % "1.1.2" % "runtime",
        // Runtime
        "org.eclipse.jetty" % "jetty-webapp" % "9.1.5.v20140505" % "container;compile",
        "org.eclipse.jetty" % "jetty-plus" % "9.1.5.v20140505" % "container",
        "org.apache.httpcomponents" % "httpcore" % "4.4.3",
        "commons-codec" % "commons-codec" % "1.10",
        "javax.servlet" % "javax.servlet-api" % "3.1.0"
      ),
      libraryDependencies ++= ProjectConfig.dependencies,
      scalacOptions in (Compile, doc) ++= Seq("-doc-root-content", baseDirectory.value+"/README.txt"),
      scalacOptions in (Compile, doc) ++= Seq("-doc-title", "Scalatra Rest API Template"),
    // the scalate part is not used currently and might be removed
      scalateTemplateConfig in Compile <<= ( sourceDirectory in Compile ) { base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty, /* default imports should be added here */
            Seq(
              Binding( "context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true,
                isImplicit = true )
            ), /* add extra bindings here */
            Some( "templates" )
          )
        )
      }

    )

  )


}
