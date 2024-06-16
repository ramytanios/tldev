## http4s-utils

A set of utils for developing microservices using [http4s](https://http4s.org/), with
[circe](https://circe.github.io/circe/) integration for `JSON` request/response interfaces.

1. HTTP server: set of basic endpoints factory methods and an http server builder.
Current factory methods exist for `GET`, `POST` and bidirectional websockets.

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

2. HTTP client: basic http client 

### TODO

### Examples 

More examples can be ran as follows 
```shell
sbt examples/runMain ServerMain # HTTP server example 
sbt examples/runMain ClientMain # HTTP client example 
```
