package tldev.core

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.monovore.decline.Opts
import com.monovore.decline.effect as de

object MyIOApp:

  trait Simple extends IOApp.Simple:
    def runMain: IO[Unit]
    override def run: IO[Unit] = IO.println(logo) *> runMain

  abstract class CommandIOApp(val name: String, val header: String)
      extends de.CommandIOApp(name, header):
    def runMain: Opts[IO[ExitCode]]
    override def main: Opts[IO[ExitCode]] = runMain.map(IO.println(logo) *> _)
