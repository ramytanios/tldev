import cats.effect.IO
import cats.effect.IOApp
import tldev.core.implicits.given_LoggerFactory_F
import tldev.http.server.Config
import tldev.http.server.EndpointFactory
import tldev.http.server.Server

import scala.concurrent.duration.*

object WebsocketMain extends IOApp.Simple:

  override def run: IO[Unit] =
    Server[IO](
      Config("localhost", 8090, 12),
      List(EndpointFactory[IO].jsonBidirectionalWebsocket[String, String](
        (in: fs2.Stream[IO, String]) => in,
        "/api"
      )),
      None,
      None
    ).run
