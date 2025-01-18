import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.*
import tldev.ConfigProvider
import tldev.core.implicits.given_LoggerFactory_F
import tldev.http.server.Config
import tldev.http.server.EndpointFactory
import tldev.http.server.Server

object Example2:

  case class Foo(x: String)
  object Foo:
    given Codec[Foo] = deriveCodec[Foo]

  case class Bar(y: Int)
  object Bar:
    given Codec[Bar] = deriveCodec[Bar]

  def run: IO[Unit] =
    ConfigProvider.yaml[IO, Config]().load.flatMap(config =>
      Server[IO](
        config,
        List(
          EndpointFactory[IO].jsonPost[Foo, Bar](
            "foo",
            foo =>
              IO.fromOption(foo.x.toIntOption)(
                new IllegalStateException("Input must be convertible to an integer")
              ).map(Bar(_))
          )
        ),
        None,
        None
      ).run
    )
