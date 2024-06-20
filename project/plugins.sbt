lazy val V = new {
  val tl = "0.6.3"
  val scalafix = "0.11.1"
  val mdoc = "2.5.2"
  val updates = "0.6.3"
}

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % V.updates)
addSbtPlugin("org.typelevel" % "sbt-typelevel" % V.tl)
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % V.scalafix)
addSbtPlugin("org.scalameta" % "sbt-mdoc" % V.mdoc)
