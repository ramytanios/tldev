lazy val V = new {
  val scalafix     = "0.14.0"
  val scalafmt     = "2.5.4"
  val updates      = "0.6.3"
  val scalajs      = "1.16.0"
  val crossproject = "1.3.2"
  val tl           = "0.7.6"
}

addSbtPlugin("com.timushev.sbt"   % "sbt-updates"              % V.updates)
addSbtPlugin("ch.epfl.scala"      % "sbt-scalafix"             % V.scalafix)
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"             % V.scalafmt)
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % V.scalajs)
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % V.crossproject)
addSbtPlugin("org.typelevel"      % "sbt-typelevel"            % V.tl)
