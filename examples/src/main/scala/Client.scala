import cats.effect.IO
import cats.effect.IOApp
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import tldev.http.client.*

import java.time.LocalDate
import scala.concurrent.duration.*

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

    val config = Config(Some(5.minutes))

    Client[IO](config).use: httpClient =>
      httpClient
        .get[FX](
          baseUrl = "https://api.frankfurter.app",
          path = Some("latest"),
          Map("from" -> "USD", "to" -> "CHF")
        )
        .flatMap: fx =>
          IO.consoleForIO.print(s"Received response ${fx.asJson.spaces2}")
