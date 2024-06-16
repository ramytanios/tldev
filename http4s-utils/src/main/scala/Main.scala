import cats.effect.IO
import cats.effect.IOApp
import cats.effect.implicits.*
import implied.interest.rates.http4s.server.Server
import implied.interest.rates.http4s.server.Config
import implied.interest.rates.http4s.server.EndpointFactory
import io.circe.Codec
import io.circe.generic.semiauto.*
import org.slf4j.LoggerFactory

object Main extends IOApp.Simple:

  case class Foo(x: String)
  object Foo:
    given Codec[Foo] = deriveCodec[Foo]

  case class Bar(y: Int)
  object Bar:
    given Codec[Bar] = deriveCodec[Bar]

  override def run: IO[Unit] =

    /** endpoints */
    val ef = EndpointFactory[IO]
    val alive = ef.jsonGet("alive", IO.pure("I am alive!"))
    val foo = ef.jsonPost[Foo, Bar]("foo", foo => IO.pure(Bar(2)))

    val endpoints = alive :: foo :: Nil

    /** config */
    val config = Config("localhost", 8090, 12)

    /** server */
    val httpServer = Server(config, endpoints)

    httpServer.run
