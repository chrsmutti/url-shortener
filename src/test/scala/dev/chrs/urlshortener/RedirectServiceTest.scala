package dev.chrs.urlshortener

import scala.collection.mutable

import cats.effect._
import cats.effect.testing.utest._
import io.circe.literal._
import org.http4s._
import org.http4s.circe._
import org.http4s.headers._
import org.http4s.implicits._
import utest._

object RedirectServiceTest extends IOTestSuite {

  private val haiku = new Haiku[IO] {
    override def get(): IO[String] = IO.pure("random-id-1")
  }

  override def tests: Tests = Tests {
    test("when called with an existing redirect should return Permanent Redirect status") {
      val rr = new RedirectRepository[IO] {
        override def get(id: String): IO[Option[Redirect]] =
          IO.pure(Some(Redirect(uri"http://example.com")))

        override def put(id: String, r: Redirect): IO[Either[RedirectRepository.Error, Redirect]] =
          throw new RuntimeException("should not be called")
      }

      val service = RedirectService[IO](rr, haiku)

      val result = for {
        response <- service.routes.run(Request(method = Method.GET, uri = uri"/existing-redirect"))
      } yield {
        assert(response.status == Status.MovedPermanently)
        assert(response.headers.get(Location) == Some(Location(uri"http://example.com")))
      }

      // OptionT[IO, ?] -> IO[Unit]
      result.value.void
    }

    test("when called with non-existing redirect should return Not Found status") {
      val rr = new RedirectRepository[IO] {
        override def get(id: String): IO[Option[Redirect]] =
          IO.pure(None)

        override def put(id: String, r: Redirect): IO[Either[RedirectRepository.Error, Redirect]] =
          throw new RuntimeException("should not be called")
      }

      val service = RedirectService[IO](rr, haiku)

      val result = for {
        response <- service.routes.run(Request(method = Method.GET, uri = uri"/existing-redirect"))
      } yield assert(response.status == Status.NotFound)

      // OptionT[IO, ?] -> IO[Unit]
      result.value.void
    }

    test("when called with a valid redirect and ID should create a redirect with that ID") {
      val list = mutable.Buffer[String]()
      val rr = new RedirectRepository[IO] {
        override def get(id: String): IO[Option[Redirect]] =
          throw new RuntimeException("should not be called")

        override def put(id: String, r: Redirect): IO[Either[RedirectRepository.Error, Redirect]] =
          IO(list.append(id)) *> IO.pure(Right(r))
      }

      val service = RedirectService[IO](rr, haiku)

      val result = for {
        response <- service.routes.run(
          Request(
            method = Method.POST,
            uri = uri"/"
          ).withEntity(json"""{ "uri": "http://example.com" }""")
        )
      } yield {
        assert(response.status == Status.Ok)
        assert(list.head == "random-id-1")
      }

      // OptionT[IO, ?] -> IO[Unit]
      result.value.void
    }

    test(
      "when called with a valid redirect wihout an ID should create a redirect with a random ID"
    ) {
      val list = mutable.Buffer[String]()
      val rr = new RedirectRepository[IO] {
        override def get(id: String): IO[Option[Redirect]] =
          throw new RuntimeException("should not be called")

        override def put(id: String, r: Redirect): IO[Either[RedirectRepository.Error, Redirect]] =
          IO(list.append(id)) *> IO.pure(Right(r))
      }

      val service = RedirectService[IO](rr, haiku)

      val result = for {
        response <- service.routes.run(
          Request(
            method = Method.POST,
            uri = uri"/creating-id"
          ).withEntity(json"""{ "uri": "http://example.com" }""")
        )
      } yield {
        assert(response.status == Status.Ok)
        assert(list.head == "creating-id")
      }

      // OptionT[IO, ?] -> IO[Unit]
      result.value.void
    }

    test("when called with an empty body should return Bad Request status") {
      val rr = new RedirectRepository[IO] {
        override def get(id: String): IO[Option[Redirect]] =
          throw new RuntimeException("should not be called")

        override def put(id: String, r: Redirect): IO[Either[RedirectRepository.Error, Redirect]] =
          throw new RuntimeException("should not be called")
      }

      val service = RedirectService[IO](rr, haiku)

      val result = for {
        response <- service.routes.run(
          Request(
            method = Method.POST,
            uri = uri"/creating-id"
          )
        )
      } yield {
        assert(response.status == Status.BadRequest)
      }

      // OptionT[IO, ?] -> IO[Unit]
      result.value.void
    }

    test("when called with a invalid redirect should return Unprocessable Entity status") {
      val rr = new RedirectRepository[IO] {
        override def get(id: String): IO[Option[Redirect]] =
          throw new RuntimeException("should not be called")

        override def put(id: String, r: Redirect): IO[Either[RedirectRepository.Error, Redirect]] =
          throw new RuntimeException("should not be called")
      }

      val service = RedirectService[IO](rr, haiku)

      val result = for {
        response <- service.routes.run(
          Request(
            method = Method.POST,
            uri = uri"/creating-id"
          ).withEntity(json"""{ }""")
        )
      } yield {
        assert(response.status == Status.UnprocessableEntity)
      }

      // OptionT[IO, ?] -> IO[Unit]
      result.value.void
    }

    test("when called for an existing redirect it should return Conflict status") {
      val rr = new RedirectRepository[IO] {
        override def get(id: String): IO[Option[Redirect]] =
          throw new RuntimeException("should not be called")

        override def put(id: String, r: Redirect): IO[Either[RedirectRepository.Error, Redirect]] =
          IO.pure(Left(RedirectRepository.Error.AlreadyExists))
      }

      val service = RedirectService[IO](rr, haiku)

      val result = for {
        response <- service.routes.run(
          Request(
            method = Method.POST,
            uri = uri"/creating-id"
          ).withEntity(json"""{ "uri": "http://example.com" }""")
        )
      } yield {
        assert(response.status == Status.Conflict)
      }

      // OptionT[IO, ?] -> IO[Unit]
      result.value.void
    }
  }

}
