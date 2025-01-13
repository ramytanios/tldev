lazy val V = new {
  val tl           = "0.6.3"
  val scalafix     = "0.11.1"
  val scalafmt     = "2.5.2"
  val mdoc         = "2.5.2"
  val updates      = "0.6.3"
  val scalajs      = "1.16.0"
  val crossproject = "1.3.2"
  val sbt          = "2.1.0"
  val actions      = "0.24.0"
  val release      = "1.4.0"
}

addSbtPlugin("com.timushev.sbt"   % "sbt-updates"              % V.updates)
addSbtPlugin("ch.epfl.scala"      % "sbt-scalafix"             % V.scalafix)
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"             % V.scalafmt)
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % V.scalajs)
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % V.crossproject)
addSbtPlugin("com.github.sbt"     % "sbt-git"                  % V.sbt)
addSbtPlugin("com.github.sbt"     % "sbt-github-actions"       % V.ghActions)
addSbtPlugin("com.github.sbt"     % "sbt-release"              % V.release)
