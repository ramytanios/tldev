package tldev.core

import cats.effect.kernel.Sync
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object implicits:

  given [F[_]: Sync]: LoggerFactory[F] = Slf4jFactory.create[F]
