## tldev

A set of utils for developing microservices using the [typelevel](https://typelevel.org/) stack.

> [!IMPORTANT]  
> The utilities provided are more or less designed for use cases of mine. For more advanced features, 
> use the relevant libraries directly!

1. **HTTP server**: set of basic endpoints factory methods and an HTTP server builder.
Current factory methods exist for `GET`, `POST` and bidirectional websockets.

> [!NOTE]  
> Three `alive`, `description` and `version` endpoints are always provided.

```scala
import cats.effect.IO
import cats.effect.IOApp
import tldev.http.server.*
import tldev.http.server
import tldev.core.implicits.given_LoggerFactory_F

object Main extends IOApp.Simple:
  override def run: IO[Unit] =
    val ef = EndpointFactory[IO]
    val helloworld = ef.jsonGet[String]("helloworld", IO.pure("Hello world!"))
    val config = server.Config("localhost", 8090, 12)
    val httpServer = Server(config, helloworld :: Nil, None, None)
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
import tldev.http.client.*
import tldev.http.client
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
    val config = client.Config.default
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
