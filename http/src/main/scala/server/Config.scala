package tldev.http.server

case class Config(
    host: String,
    port: Int,
    maxConnections: Int,
    prefix: Option[String] = Option("/api")
)
