package dev.chrs.urlshortener

import cats.effect._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._

// TODO: improve uri validation (only http/https, no relative path)
case class Redirect(uri: Uri)

case object Redirect {
  implicit def jsonDecoder[F[_]: Sync]: EntityDecoder[F, Redirect] = jsonOf[F, Redirect]
}
