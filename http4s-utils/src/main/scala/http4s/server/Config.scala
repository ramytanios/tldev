package implied.interest.rates.http4s.server

case class Config(
    host: String,
    port: Int,
    maxConnections: Int,
    prefix: Option[String] = Some("api")
)

object Config:
  def local: Config = Config("localhost", 8090, 32)
