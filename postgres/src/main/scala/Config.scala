package tldev.postgres

import pureconfig.ConfigReader

case class Config(
    host: String,
    port: Int,
    user: String,
    pass: String,
    db: String,
    maxConnections: Int
)

object Config:
  given ConfigReader[Config] = ConfigReader.derived[Config]
