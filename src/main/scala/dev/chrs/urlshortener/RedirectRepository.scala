package dev.chrs.urlshortener

import scala.collection.mutable

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.implicits._

import RedirectRepository.Error
import RedirectRepository.Error._

object RedirectRepository {
  def apply[F[_]](implicit F: Effect[F]) = new RedirectRepositoryImpl

  sealed abstract class Error extends Product with Serializable
  object Error {
    case object AlreadyExists extends Error
  }
}

trait RedirectRepository[F[_]] {
  def get(id: String): F[Option[Redirect]]
  def put(id: String, r: Redirect): F[Either[Error, Redirect]]
}

class RedirectRepositoryImpl[F[_]](implicit F: Effect[F]) extends RedirectRepository[F] {

  // TODO: use a real database
  val db: mutable.Map[String, Uri] = mutable.Map("http4s" -> uri"https://http4s.org")

  def put(id: String, r: Redirect): F[Either[Error, Redirect]] = {
    F.pure(db.get(id)).map {
      case Some(_) => Left(AlreadyExists)
      case None =>
        db.put(id, r.uri)
        Right(r)
    }
  }

  def get(id: String): F[Option[Redirect]] = F.pure(db.get(id).map(Redirect.apply))

}
