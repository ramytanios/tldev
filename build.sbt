Global / onChangedBuildSource := ReloadOnSourceChanges
// Global / resolvers += "GitHub Package Registry" at "https://maven.pkg.github.com/ramytanios/tldev"

lazy val scala3 = "3.3.1"
ThisBuild / scalaVersion       := scala3
ThisBuild / crossScalaVersions := Seq(scala3)

ThisBuild / organization     := "io.github.ramytanios"
ThisBuild / organizationName := "ramytanios"
ThisBuild / publishTo := Some(
  "GitHub Packages" at "https://maven.pkg.github.com/ramytanios/tldev"
)
ThisBuild / publishMavenStyle := true
ThisBuild / credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "ramytanios",
  sys.env.getOrElse("GH_TOKEN", "")
)

ThisBuild / githubWorkflowEnv := Map("GH_TOKEN" -> "${{ secrets.GH_TOKEN }}")

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

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

lazy val root = project.in(file("."))
  .aggregate(http, core.jvm, core.js)
  .settings(publish / skip := true, git.useGitDescribe := true)
  .enablePlugins(GitVersioning)

lazy val http = project
  .in(file("http"))
  .enablePlugins(GitVersioning)
  .settings(
    name               := "tldev-http",
    git.useGitDescribe := true,
    fork               := true,
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
  .enablePlugins(GitVersioning)
  .settings(
    name               := "tldev-core",
    fork               := true,
    git.useGitDescribe := true,
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

lazy val examples = project
  .in(file("examples"))
  .settings(name := "examples", fork := true, publishArtifact := false)
  .dependsOn(http, core.jvm)
