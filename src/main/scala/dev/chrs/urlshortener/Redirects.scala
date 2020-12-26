package dev.chrs.urlshortener

import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.log4cats._
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s._

import Redirects.Error
import Redirects.Error._
import Redirect.AbsoluteUri

object Redirects {
  sealed abstract class Error extends Product with Serializable
  object Error {
    case object AlreadyExists extends Error
  }
}

trait Redirects[F[_]] {
  def get(id: String): F[Option[Redirect]]
  def put(id: String, r: Redirect): F[Either[Error, Redirect]]
}

object LiveRedirects {
  def make[F[_]: ConcurrentEffect: ContextShift]: F[LiveRedirects[F]] =
    Sync[F].delay(new LiveRedirects[F])
}

class LiveRedirects[F[_]: ConcurrentEffect: ContextShift] private extends Redirects[F] {

  private implicit def unsafeLogger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  private lazy val redis: Resource[F, RedisCommands[F, String, String]] =
    Redis[F].utf8("redis://redis")

  private def key(id: String) = s"redirect:$id"

  def put(id: String, r: Redirect): F[Either[Error, Redirect]] = {
    redis.use { cmd =>
      cmd.get(key(id)).flatMap {
        case Some(_) => Sync[F].pure(Left(AlreadyExists))
        case None =>
          cmd.set(key(id), r.uri.toString()) *>
            Sync[F].pure(Right(r))
      }
    }
  }

  def get(id: String): F[Option[Redirect]] =
    redis.use(
      _.get(key(id)).map(
        _.flatMap(Uri.fromString(_).toOption).map(uri => Redirect(AbsoluteUri(uri)))
      )
    )

}
