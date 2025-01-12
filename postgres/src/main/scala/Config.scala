package tldev.postgres

case class Config(
    host: String,
    port: Int,
    user: String,
    pass: String,
    db: String,
    maxConnections: Int
)
