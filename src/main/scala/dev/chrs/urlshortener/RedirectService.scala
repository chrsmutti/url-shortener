package dev.chrs.urlshortener

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._

import RedirectRepository.Error._

object RedirectService {
  def apply[F[_]: Sync](rr: RedirectRepository[F], haiku: Haiku[F]) =
    new RedirectService[F](rr, haiku)
}

class RedirectService[F[_]: Sync](rr: RedirectRepository[F], haiku: Haiku[F]) extends Http4sDsl[F] {

  def routes: HttpRoutes[F] = HttpRoutes
    .of[F] {
      // Posting to root should create a redirect with a random ID and return that ID in the location
      // header.
      case r @ POST -> Root =>
        r.decode[Redirect] { r =>
          val result = for {
            id <- haiku.get()
            result <- rr.put(id, r)
          } yield result

          result.flatMap {
            case Right(r)            => Ok().map(_.putHeaders(Location(r.uri)))
            case Left(AlreadyExists) => Conflict()
          }
        }

      // Navigating to an existing ID should return permanent redirect to the underlying URL.
      case GET -> Root / id =>
        rr.get(id).flatMap {
          case Some(Redirect(uri)) => MovedPermanently(Location(uri))
          case None                => NotFound()
        }

      // Posting to an ID with a Redirect should create a redirect with that ID if it's not still in use,
      // otherwise should return status Conflict.
      case r @ POST -> Root / id =>
        r.decode[Redirect] { r =>
          rr.put(id, r).flatMap {
            case Right(_)            => Ok()
            case Left(AlreadyExists) => Conflict()
          }
        }

      // Any other method supplying an ID shouldn't be allowed.
      case _ -> Root / _ => MethodNotAllowed(Allow(GET, POST))
    }

}
