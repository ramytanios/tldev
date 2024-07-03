// package tldev.core.utils
//
// import cats.effect.kernel.Async
// import cats.syntax.all.*
// import scala.concurrent.duration.*
// import cats.effect.kernel.Ref
// import cats.effect.kernel.Fiber
// import cats.effect.std.Supervisor
// import cats.effect.implicits.*
// import cats.effect.std.Mutex
//
// sealed trait ExpiryCache[F[_], K, V]:
//
//   def addOrUpdate(key: K, value: V, expiresIn: FiniteDuration): F[Unit]
//
//   def get(key: K): F[Option[V]]
//
//   def remove(key: K): F[Unit]
//
//   def clear: F[Unit]
//
//   def updates: fs2.Stream[F, Map[K, V]]
//
//   def snapshot: F[Map[K, V]]
//
//   def resetExpiry(key: K, expiresIn: FiniteDuration): F[Unit]
//
// object ExpiryCache:
//   def apply[F[_]: Async, K, V] =
//     for
//       cache <- Ref.of(Map.empty[K, V]).toResource
//       fibz <- Ref.of(Map.empty[K, Fiber[F, Throwable, Unit]]).toResource
//       mutex <- Mutex[F].toResource
//       supervisor <- Supervisor[F]
//     yield new ExpiryCache[F, K, V]:
//
//       override def add(key: K, value: V, ttl: FiniteDuration): F[Unit] =
//         mutex.lock.surround(
//           supervisor
//             .supervise:
//               for
//                 _ <- cache.update(_ + (key -> value))
//                 _ <- Async[F].sleep(ttl)
//                 _ <- cache.update(_ - key)
//               yield ()
//             .flatMap: fib =>
//               fibz.update(_ + (key -> fib))
//         )
//       override def get(key: K): F[Option[V]] = cache.get.map(_.get(key))
//
//       override def clear: F[Unit] =
//         cache.set(Map.empty[K, V]) *> fibz.get.flatMap(
//           _.toList.parUnorderedTraverse((_, fib) => fib.cancel).void
//         )
//
//       override def resetExpiry(key: K, expiresIn: FiniteDuration): F[Unit] = ???
//       override def updates: fs2.Stream[F, Map[K, V]] = ???
//       override def remove(key: K): F[Unit] = ???
//       override def snapshot: F[Map[K, V]] = ???
