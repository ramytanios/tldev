Global / onChangedBuildSource := ReloadOnSourceChanges
Global / resolvers += "Sonatype S01 OSS Snapshots".at(
  "https://s01.oss.sonatype.org/content/repositories/snapshots"
)

ThisBuild / tlBaseVersion := "0.0"

lazy val scala3 = "3.3.1"
ThisBuild / scalaVersion       := scala3
ThisBuild / crossScalaVersions := Seq(scala3)
ThisBuild / semanticdbEnabled  := true
ThisBuild / semanticdbVersion  := scalafixSemanticdb.revision

ThisBuild / organization            := "io.github.ramytanios"
ThisBuild / organizationName        := "ramytanios"
ThisBuild / startYear               := Some(2024)
ThisBuild / tlSonatypeUseLegacyHost := false

ThisBuild / tlFatalWarnings        := false
ThisBuild / tlCiHeaderCheck        := false
ThisBuild / tlCiDependencyGraphJob := false
ThisBuild / tlCiScalafmtCheck      := true
ThisBuild / tlCiScalafixCheck      := true

ThisBuild / developers := List(
  tlGitHubDev("ramytanios", "Ramy Tanios")
)

lazy val V = new {
  val circe         = "0.14.6"
  val cats          = "2.12.0"
  val fs2           = "3.10.2"
  val fs2data       = "1.11.0"
  val catsEffect    = "3.5.4"
  val kittens       = "3.2.0"
  val mouse         = "1.3.0"
  val catsTime      = "0.5.1"
  val scalaJavaTime = "2.5.0"
  val ff4s          = "0.24.0"
  val http4s        = "0.23.27"
  val ciris         = "3.6.0"
  val log4cats      = "2.7.0"
  val logback       = "1.2.10"
  val literally     = "1.2.0"
}

lazy val root = tlCrossRootProject.aggregate(http, core.jvm, core.js, docs, examples)

lazy val http = project
  .in(file("http"))
  .settings(
    name := "http",
    fork := true,
    libraryDependencies ++=
      Seq(
        "org.typelevel" %% "log4cats-slf4j"      % V.log4cats,
        "ch.qos.logback" % "logback-classic"     % V.logback,
        "ch.qos.logback" % "logback-core"        % V.logback,
        "io.circe"      %% "circe-core"          % V.circe,
        "io.circe"      %% "circe-generic"       % V.circe,
        "io.circe"      %% "circe-literal"       % V.circe,
        "io.circe"      %% "circe-parser"        % V.circe,
        "org.typelevel" %% "cats-core"           % V.cats,
        "org.typelevel" %% "cats-effect"         % V.catsEffect,
        "org.typelevel" %% "cats-effect-std"     % V.catsEffect,
        "co.fs2"        %% "fs2-core"            % V.fs2,
        "co.fs2"        %% "fs2-io"              % V.fs2,
        "org.typelevel" %% "mouse"               % V.mouse,
        "org.http4s"    %% "http4s-dsl"          % V.http4s,
        "org.http4s"    %% "http4s-circe"        % V.http4s,
        "org.http4s"    %% "http4s-ember-server" % V.http4s,
        "org.http4s"    %% "http4s-ember-client" % V.http4s
      )
  )
  .dependsOn(core.jvm)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .in(file("core"))
  .settings(
    name := "core",
    fork := true,
    libraryDependencies ++=
      Seq(
        "org.typelevel" %% "log4cats-slf4j"  % V.log4cats,
        "ch.qos.logback" % "logback-classic" % V.logback,
        "ch.qos.logback" % "logback-core"    % V.logback,
        "org.typelevel" %% "cats-core"       % V.cats,
        "org.typelevel" %% "cats-effect"     % V.catsEffect,
        "org.typelevel" %% "cats-effect-std" % V.catsEffect,
        "co.fs2"        %% "fs2-core"        % V.fs2,
        "co.fs2"        %% "fs2-io"          % V.fs2,
        "org.gnieh"     %% "fs2-data-csv"    % V.fs2data,
        "org.typelevel" %% "literally"       % V.literally
      )
  )

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(MdocPlugin, NoPublishPlugin)
  .settings(
    mdocIn        := file("docs"),
    mdocOut       := file("."),
    mdocVariables := Map("VERSION" -> version.value)
  )
  .dependsOn(http, core.jvm)

lazy val examples = project
  .in(file("examples"))
  .enablePlugins(NoPublishPlugin)
  .settings(
    name := "examples",
    fork := true
  )
  .dependsOn(http, core.jvm)
