package tldev.http.server

import pureconfig.ConfigReader

case class Config(
    host: String,
    port: Int,
    maxConnections: Int,
    prefix: Option[String] = Option("/api")
)

object Config:
  given ConfigReader[Config] = ConfigReader.derived[Config]
