
import cats.effect.IO
import cats.effect.IOApp
import tldev.core.implicits.given_LoggerFactory_F
import tldev.http.server.Config
import tldev.http.server.EndpointFactory
import tldev.http.server.Server

import scala.concurrent.duration.*

object WebsocketMain extends IOApp.Simple:

  override def run: IO[Unit] =

    val ef = EndpointFactory[IO]

    // websocket
    val ws = ef.jsonBidirectionalWebsocket[String, String](
      _.printlns,
      fs2.Stream
        .fixedDelay[IO](5.seconds)
        .map: _ =>
          "Hello World!"
    )

    // config
    val config = Config("localhost", 8090, 12)

    // server
    val httpServer = Server[IO](config, ws :: Nil, None, None)

    httpServer.run
