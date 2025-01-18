package tldev.http.client

import scala.concurrent.duration._

case class Config(timeout: Option[FiniteDuration] = Option(5.minutes))
