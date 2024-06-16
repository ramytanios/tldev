import cats.effect.IO
import cats.effect.IOApp
import http4sutils.server.Server
import http4sutils.server.Config
import http4sutils.server.EndpointFactory
import io.circe.Codec
import io.circe.generic.semiauto.*

object ServerMain extends IOApp.Simple:

  case class Foo(x: String)
  object Foo:
    given Codec[Foo] = deriveCodec[Foo]

  case class Bar(y: Int)
  object Bar:
    given Codec[Bar] = deriveCodec[Bar]

  override def run: IO[Unit] =

    // endpoints
    val ef = EndpointFactory[IO]
    val foo = ef.jsonPost[Foo, Bar]("foo", foo => IO.pure(Bar(2)))

    val endpoints = foo :: Nil

    // config
    val config = Config("localhost", 8090, 12)

    // server
    val httpServer = Server[IO](config, endpoints, None, None)

    httpServer.run
