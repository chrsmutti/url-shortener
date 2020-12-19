package dev.chrs.urlshortener

import cats.effect._
import cats.effect.testing.utest._
import dev.chrs.urlshortener.RedirectRepository.Error.AlreadyExists
import org.http4s.implicits._
import utest._

object RedirectRepositoryTests extends IOTestSuite {

  private val rr = RedirectRepository[IO]

  override def tests: Tests = Tests {
    test("trying to get a non-existing key should return None") {
      for {
        result <- rr.get("non-existing")
      } yield assert(result.isEmpty)
    }

    test("inserting a key should work and return the inserted value") {
      for {
        result <- rr.put("insert-testing-1", Redirect(uri"http://example.com"))
      } yield assert(result == Right(Redirect(uri"http://example.com")))
    }

    test("inserting a key twice should not work") {
      for {
        _ <- rr.put("insert-testing-2", Redirect(uri"http://example.com"))
        result <- rr.put("insert-testing-2", Redirect(uri"http://example.com"))
      } yield assert(result == Left(AlreadyExists))
    }

    test("inserting a key should persist it") {
      for {
        _ <- rr.put("insert-testing-3", Redirect(uri"http://example.com"))
        result <- rr.get("insert-testing-3")
      } yield assert(result == Some(Redirect(uri"http://example.com")))
    }
  }

}
