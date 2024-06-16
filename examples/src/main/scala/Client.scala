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
        .get[FX]("https://api.frankfurter.app/latest?from=USD&to=CHF")
        .flatMap: fx =>
          IO.consoleForIO.print(s"Received response ${fx.asJson.spaces2}")
