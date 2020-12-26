package dev.chrs.urlshortener

import cats.effect._
import io.circe._
import org.http4s._
import org.http4s.circe._

import Redirect.AbsoluteUri

case class Redirect(uri: AbsoluteUri)

case object Redirect {
  case class AbsoluteUri(uri: Uri)

  private implicit def decoder[F[_]: Sync]: Decoder[Redirect] = Decoder.instance[Redirect] { c =>
    c.downField("url").as[Uri].flatMap {
      case uri if uri.scheme.isDefined && uri.authority.isDefined =>
        Right(Redirect(AbsoluteUri(uri)))
      case _ =>
        Left(DecodingFailure("url cannot be relative.", c.history))
    }
  }

  implicit def jsonDecoder[F[_]: Sync]: EntityDecoder[F, Redirect] = jsonOf[F, Redirect]
}
