Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val scala3 = "3.3.1"

ThisBuild / scalaVersion := scala3
ThisBuild / crossScalaVersions := Seq(scala3)
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val V = new {
  val circe = "0.14.6"
  val cats = "2.12.0"
  val fs2 = "3.10.2"
  val catsEffect = "3.5.4"
  val kittens = "3.2.0"
  val literally = "1.1.0"
  val mouse = "1.3.0"
  val catsTime = "0.5.1"
  val scalaJavaTime = "2.5.0"
  val ff4s = "0.24.0"
  val http4s = "0.23.27"
  val ciris = "3.6.0"
  val log4cats = "2.7.0"
}

lazy val root =
  (project in file(".")).aggregate(dtos.jvm, backend, frontend)

lazy val dtos = crossProject(JSPlatform, JVMPlatform)
  .in(file("dtos"))
  .settings(
    name := "implied-interest-rates-dtos",
    scalacOptions -= "-Xfatal-warnings",
    libraryDependencies ++=
      Seq(
        "io.circe" %% "circe-core" % V.circe,
        "io.circe" %% "circe-generic" % V.circe,
        "io.circe" %% "circe-literal" % V.circe,
        "io.circe" %% "circe-parser" % V.circe
      )
  )

lazy val frontend = project
  .in(file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "implied-interest-rates-frontend",
    scalacOptions -= "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % V.scalaJavaTime,
      "org.typelevel" %%% "mouse" % V.mouse,
      "io.github.buntec" %%% "ff4s" % V.ff4s
    )
  )
  .dependsOn(dtos.js)

lazy val backend = project
  .in(file("backend"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "implied-interest-rates-backend",
    fork := true,
    libraryDependencies ++=
      Seq(
        "io.circe" %% "circe-core" % V.circe,
        "io.circe" %% "circe-generic" % V.circe,
        "io.circe" %% "circe-literal" % V.circe,
        "io.circe" %% "circe-parser" % V.circe,
        "org.typelevel" %% "cats-core" % V.cats,
        "org.typelevel" %% "cats-effect" % V.catsEffect,
        "org.typelevel" %% "cats-effect-std" % V.catsEffect,
        "co.fs2" %% "fs2-core" % V.fs2,
        "co.fs2" %% "fs2-io" % V.fs2,
        "org.typelevel" %% "kittens" % V.kittens,
        "org.typelevel" %% "cats-time" % V.catsTime,
        "org.typelevel" %% "literally" % V.literally,
        "org.typelevel" %% "mouse" % V.mouse,
        "org.http4s" %% "http4s-dsl" % V.http4s,
        "org.http4s" %% "http4s-circe" % V.http4s,
        "org.http4s" %% "http4s-ember-server" % V.http4s,
        "org.http4s" %% "http4s-ember-client" % V.http4s,
        "org.typelevel" %% "log4cats-core" % V.log4cats,
        "org.typelevel" %% "log4cats-slf4j" % V.log4cats
      ),
    scalacOptions -= "-Xfatal-warnings"
  )
  .dependsOn(dtos.jvm)
