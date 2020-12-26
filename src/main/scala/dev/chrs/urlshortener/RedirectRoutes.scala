package dev.chrs.urlshortener

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._

import Redirects.Error._
import Redirect.AbsoluteUri

object RedirectRoutes {
  def make[F[_]: Sync](rr: Redirects[F], haiku: Haikus[F]): F[RedirectRoutes[F]] =
    Sync[F].delay(new RedirectRoutes[F](rr, haiku))
}

class RedirectRoutes[F[_]: Sync] private (rr: Redirects[F], haiku: Haikus[F]) extends Http4sDsl[F] {

  /**
   * Lift ID or create one using Haikus, parse it into an Uri so it can
   * be returned in the Location header later.
   *
   * @param id ID to lift, if existing, or None to create one.
   * @param f Function to call with the ID.
   * @return The result of the function, or an InternalServerError in case the
   *         id is not parseable to an Uri.
   */
  private def withId(id: Option[String])(f: (String, Uri) => F[Response[F]]) = {
    id.map(Sync[F].pure).getOrElse(haiku.get()).map(id => (id, Uri.fromString(id))).flatMap {
      case (id, Right(uri)) => f(id, uri)
      case (_, Left(_))     => InternalServerError()
    }
  }

  def routes: HttpRoutes[F] = HttpRoutes
    .of[F] {
      // Posting to root should create a redirect with a random ID and return that ID in the location
      // header.
      case r @ POST -> Root =>
        r.decode[Redirect] { r =>
          withId(None) { (id, uri) =>
            rr.put(id, r).flatMap {
              case Right(_)            => Ok().map(_.putHeaders(Location(uri)))
              case Left(AlreadyExists) => Conflict()
            }
          }
        }

      // Navigating to an existing ID should return permanent redirect to the underlying URL.
      case GET -> Root / id =>
        rr.get(id).flatMap {
          case Some(Redirect(AbsoluteUri(uri))) => MovedPermanently(Location(uri))
          case None                             => NotFound()
        }

      // Posting to an ID with a Redirect should create a redirect with that ID if it's not still in use,
      // otherwise should return status Conflict.
      case r @ POST -> Root / id =>
        r.decode[Redirect] { r =>
          withId(Some(id)) { (id, uri) =>
            rr.put(id, r).flatMap {
              case Right(_)            => Ok().map(_.putHeaders(Location(uri)))
              case Left(AlreadyExists) => Conflict()
            }
          }
        }

      // Any other method supplying an ID shouldn't be allowed.
      case _ -> Root / _ => MethodNotAllowed(Allow(GET, POST))
    }

}
