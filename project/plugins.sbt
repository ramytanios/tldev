lazy val V = new {
  val tl = "0.6.3"
  val scalafix = "0.11.1"
  val mdoc = "2.5.2"
  val updates = "0.6.3"
  val scalajs = "1.16.0"
  val crossproject = "1.3.2"
}

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % V.updates)
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % V.scalafix)
addSbtPlugin("org.scala-js" % "sbt-scalajs" % V.scalajs)
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % V.crossproject)
