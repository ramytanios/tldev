import org.typelevel.scalacoptions
import org.typelevel.scalacoptions.ScalacOption

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val scala3 = "3.6.2"
lazy val java   = JavaSpec.zulu("21")

lazy val gh = new {
  val resolver = "GitHub Packages" at "https://maven.pkg.github.com/ramytanios/tldev"
  val realm    = "GitHub Package Registry"
  val host     = "maven.pkg.github.com"
  val username = "ramytanios"
  val token    = sys.env.getOrElse("GH_TOKEN", "")
}

ThisBuild / scalaVersion       := scala3
ThisBuild / crossScalaVersions := Seq(scala3)

ThisBuild / organization      := "io.github.ramytanios"
ThisBuild / organizationName  := "ramytanios"
ThisBuild / publishTo         := Some(gh.resolver)
ThisBuild / publishMavenStyle := true
ThisBuild / credentials += Credentials(gh.realm, gh.host, gh.host, gh.token)

ThisBuild / githubWorkflowJavaVersions := Seq(java)
ThisBuild / githubWorkflowEnv          := Map("GH_TOKEN" -> "${{ secrets.GH_TOKEN }}")
ThisBuild / githubWorkflowPublishCond := Some(
  "contains(github.event.head_commit.message, '[publish]')"
)
ThisBuild / githubWorkflowGeneratedCI := WorkflowJob(
  id = "quality-check",
  name = "Code quality checks",
  steps = WorkflowStep.CheckoutFull ::
    WorkflowStep.SetupJava((ThisBuild / githubWorkflowJavaVersions).value.toList) :::
    WorkflowStep.SetupSbt() ::
    WorkflowStep.Sbt(List("scalafmtCheckAll"), name = Some("Scala fmt")) ::
    WorkflowStep.Sbt(List("scalafixAll --check"), name = Some("Scala fix")) ::
    Nil,
  scalas = List(scala3),
  javas = List(java),
  matrixFailFast = Some(true)
) +: (ThisBuild / githubWorkflowGeneratedCI).value

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / tpolecatExcludeOptions := Set(
  ScalacOption("-Xfatal-warnings", (_: scalacoptions.ScalaVersion) => true)
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
  val skunk         = "0.6.4"
  val pureConfig    = "0.17.8"
  val decline       = "2.5.0"
}

lazy val root = project.in(file("."))
  .aggregate(http, postgres, examples, core.jvm, core.js)
  .settings(publish / skip := true, git.useGitDescribe := true)
  .enablePlugins(GitVersioning)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .in(file("core"))
  .enablePlugins(GitVersioning)
  .settings(
    name               := "tldev-core",
    git.useGitDescribe := true,
    libraryDependencies ++=
      Seq(
        "ch.qos.logback"         % "logback-classic"           % V.logback,
        "ch.qos.logback"         % "logback-core"              % V.logback,
        "org.typelevel"         %% "log4cats-slf4j"            % V.log4cats,
        "org.typelevel"         %% "cats-core"                 % V.cats,
        "org.typelevel"         %% "cats-effect"               % V.catsEffect,
        "org.typelevel"         %% "cats-effect-std"           % V.catsEffect,
        "co.fs2"                %% "fs2-core"                  % V.fs2,
        "co.fs2"                %% "fs2-io"                    % V.fs2,
        "org.gnieh"             %% "fs2-data-csv"              % V.fs2data,
        "org.typelevel"         %% "literally"                 % V.literally,
        "com.github.pureconfig" %% "pureconfig-core"           % V.pureConfig,
        "com.github.pureconfig" %% "pureconfig-cats"           % V.pureConfig,
        "com.github.pureconfig" %% "pureconfig-yaml"           % V.pureConfig,
        "com.github.pureconfig" %% "pureconfig-cats-effect"    % V.pureConfig,
        "com.github.pureconfig" %% "pureconfig-generic-base"   % V.pureConfig,
        "com.github.pureconfig" %% "pureconfig-generic-scala3" % V.pureConfig,
        "com.monovore"          %% "decline"                   % V.decline,
        "com.monovore"          %% "decline-effect"            % V.decline
      )
  )

lazy val http = project
  .in(file("http"))
  .enablePlugins(GitVersioning)
  .settings(
    name               := "tldev-http",
    git.useGitDescribe := true,
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

lazy val postgres = project
  .in(file("postgres"))
  .enablePlugins(GitVersioning)
  .settings(
    name               := "tldev-postgres",
    git.useGitDescribe := true,
    libraryDependencies ++=
      Seq(
        "org.tpolecat" %% "skunk-core"  % V.skunk,
        "org.tpolecat" %% "skunk-circe" % V.skunk
      )
  )
  .dependsOn(core.jvm)

lazy val examples = project
  .in(file("examples"))
  .settings(
    name := "examples",
    publishArtifact := false,
    libraryDependencies ++= List(
      "com.monovore" %% "decline"        % V.decline,
      "com.monovore" %% "decline-effect" % V.decline
    )
  )
  .dependsOn(http, core.jvm)
