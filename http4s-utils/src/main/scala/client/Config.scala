package http4sutils.client

import scala.concurrent.duration.*

case class Config(timeout: FiniteDuration)

object Config:
  def default: Config = Config(timeout = 5.minutes)
