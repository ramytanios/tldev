## tldev

A set of utils for developing microservices using [http4s](https://http4s.org/), with
[circe](https://circe.github.io/circe/) integration for `JSON` request/response interfaces,
avoiding boilerplate code on each project 🚀

> [!IMPORTANT]  
> The utilities provided are more or less designed for use cases of mine. For more advanced features, 
> use [http4s](https://http4s.org/) directly!

1. **HTTP server**: set of basic endpoints factory methods and an HTTP server builder.
Current factory methods exist for `GET`, `POST` and bidirectional websockets.

> [!NOTE]  
> Three `alive`, `description` and `version` endpoints are always provided.

```scala
import cats.effect.IO
import cats.effect.IOApp
import http4sutils.server.*

object Main extends IOApp.Simple:
  override def run: IO[Unit] =
    val ef = EndpointFactory[IO]
    val helloworld = ef.jsonGet[String]("helloworld", IO.pure("Hello world!"))
    val config = Config("localhost", 8090, 12)
    val httpServer = Server(config, endpoints, None, None)
    httpServer.run
```

```shell
curl localhost:8090/api/alive
curl localhost:8090/api/helloworld
```

2. **HTTP client**: basic http client 

```scala 
import cats.effect.IO
import cats.effect.IOApp
import http4sutils.client.*
import java.time.LocalDate
import io.circe.*
import io.circe.syntax.*
import io.circe.generic.semiauto.*

object ClientMain extends IOApp.Simple:

  case class FX(
      amount: Double,
      date: LocalDate,
      base: String,
      rates: Map[String, Double]
  )
  object FX:
    given Codec[FX] = deriveCodec[FX]

  override def run: IO[Unit] =
    val config = Config.default
    Client[IO](config).use: httpClient =>
      httpClient
        .get[FX](
          baseUrl = "https://api.frankfurter.app",
          path = Some("latest"),
          Map("from" -> "USD", "to" -> "CHF")
        )
        .flatMap: fx =>
          IO.consoleForIO.print(s"Received response ${fx.asJson.spaces2}")
```

### Examples 

More examples can be ran:
```shell
sbt examples/runMain ServerMain # HTTP server example 
sbt examples/runMain ClientMain # HTTP client example 
sbt examples/runMain WebsocketMain # Websocket example
```
