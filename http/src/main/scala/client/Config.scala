package tldev.http.client

import scala.concurrent.duration.*

case class Config(timeout: Option[FiniteDuration] = Option(5.minutes))
