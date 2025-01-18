package tldev.http.client

import pureconfig.ConfigReader

import scala.concurrent.duration.*

case class Config(timeout: Option[FiniteDuration] = Option(5.minutes))

object Config:
  given ConfigReader[Config] = ConfigReader.derived[Config]
