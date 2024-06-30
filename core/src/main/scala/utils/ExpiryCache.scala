package tldev.core.utils

import cats.effect.kernel.Async

import scala.concurrent.duration.*

trait ExpiryCache[F[_], K, V]:
  def add(key: K, value: V, expiry: FiniteDuration): F[Unit]
  def get(key: K): F[Option[V]]
  def clear: F[Unit]

object ExpiryCache:
  def apply[F[_]: Async, K, V]: ExpiryCache[F, K, V] =
    new ExpiryCache[F, K, V]:
      override def add(key: K, value: V, expiry: FiniteDuration): F[Unit] = ???
      override def get(key: K): F[Option[V]] = ???
      override def clear: F[Unit] = ???
