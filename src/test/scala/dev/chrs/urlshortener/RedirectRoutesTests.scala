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

import Redirects.Error.AlreadyExists
import Redirect.AbsoluteUri

object RedirectRoutesTests extends IOTestSuite {

  private def existingRedirect: TestRedirects = new TestRedirects {
    override def get(id: String): IO[Option[Redirect]] =
      IO.pure(Some(Redirect(AbsoluteUri(uri"http://example.com"))))
  }

  private def insertRedirect(list: mutable.Buffer[String]): TestRedirects = new TestRedirects {
    override def put(id: String, r: Redirect): IO[Either[Redirects.Error, Redirect]] =
      IO(list.append(id)) *> IO.pure(Right(r))
  }

  override def tests: Tests = Tests {
    test("when called with non-existing redirect should return Not Found status") {
      for {
        service <- RedirectRoutes.make(new TestRedirects, new TestHaikus)
        response <- service.routes
          .run(Request(method = Method.GET, uri = uri"/existing-redirect"))
          .value
      } yield for {
        response <- response
        _ = assert(response.status == Status.NotFound)
      } yield response
    }

    test("when called with an existing redirect should return Permanent Redirect status") {
      for {
        service <- RedirectRoutes.make(existingRedirect, new TestHaikus)
        response <- service.routes
          .run(Request(method = Method.GET, uri = uri"/existing-redirect"))
          .value
      } yield for {
        response <- response
        _ = assert(response.status == Status.MovedPermanently)
        _ = assert(response.headers.get(Location) == Some(Location(uri"http://example.com")))
      } yield response
    }

    test(
      "when called with a valid redirect wihout an ID should create a redirect with a random ID"
    ) {
      val list = mutable.Buffer[String]()

      for {
        service <- RedirectRoutes.make(insertRedirect(list), new TestHaikus)
        response <- service.routes
          .run(
            Request(
              method = Method.POST,
              uri = uri"/"
            ).withEntity(json"""{ "url": "http://example.com" }""")
          )
          .value
      } yield for {
        response <- response
        _ = assert(response.status == Status.Ok)
        _ = assert(list.head == "random-id-1")

        uri <- Uri.fromString("random-id-1").toOption
        _ = assert(response.headers.exists(_ == Location(uri)))
      } yield response
    }

    test(
      "when called with a valid redirect and ID should create a redirect with that ID"
    ) {
      val list = mutable.Buffer[String]()

      for {
        service <- RedirectRoutes.make(insertRedirect(list), new TestHaikus)
        response <- service.routes
          .run(
            Request(
              method = Method.POST,
              uri = uri"/creating-id"
            ).withEntity(json"""{ "url": "http://example.com" }""")
          )
          .value
      } yield for {
        response <- response
        _ = assert(response.status == Status.Ok)
        _ = assert(list.head == "creating-id")

        uri <- Uri.fromString("creating-id").toOption
        _ = assert(response.headers.exists(_ == Location(uri)))
      } yield response
    }

    test("when called with an empty body should return Bad Request status") {
      for {
        service <- RedirectRoutes.make(new TestRedirects, new TestHaikus)
        response <- service.routes
          .run(
            Request(method = Method.POST, uri = uri"/creating-id")
          )
          .value
      } yield for {
        response <- response
        _ = assert(response.status == Status.BadRequest)
      } yield response
    }

    test("when called with a invalid redirect should return Unprocessable Entity status") {
      for {
        service <- RedirectRoutes.make(new TestRedirects, new TestHaikus)
        response <- service.routes
          .run(
            Request(
              method = Method.POST,
              uri = uri"/creating-id"
            ).withEntity(json"""{ }""")
          )
          .value
      } yield for {
        response <- response
        _ = assert(response.status == Status.UnprocessableEntity)
      } yield response
    }

    test("when called for an existing redirect it should return Conflict status") {
      for {
        service <- RedirectRoutes.make(new TestRedirects, new TestHaikus)
        response <- service.routes
          .run(
            Request(
              method = Method.POST,
              uri = uri"/creating-id"
            ).withEntity(json"""{ "url": "http://example.com" }""")
          )
          .value
      } yield for {
        response <- response
        _ = assert(response.status == Status.Conflict)
      } yield response
    }

    test("when called with a relative urls should return Unprocessable Entity status") {
      for {
        service <- RedirectRoutes.make(new TestRedirects, new TestHaikus)
        response <- service.routes
          .run(
            Request(
              method = Method.POST,
              uri = uri"/creating-id"
            ).withEntity(json"""{ "url": "/relative" }""")
          )
          .value
      } yield for {
        response <- response
        _ = assert(response.status == Status.UnprocessableEntity)
      } yield response
    }

    test("when called without a scheme should return Unprocessable Entity status") {
      for {
        service <- RedirectRoutes.make(new TestRedirects, new TestHaikus)
        response <- service.routes
          .run(
            Request(
              method = Method.POST,
              uri = uri"/creating-id"
            ).withEntity(json"""{ "url": "example.com" }""")
          )
          .value
      } yield for {
        response <- response
        _ = assert(response.status == Status.UnprocessableEntity)
      } yield response
    }
  }

  private class TestHaikus extends Haikus[IO] {
    def get(): IO[String] = IO.pure("random-id-1")
  }

  private class TestRedirects extends Redirects[IO] {
    def get(id: String): IO[Option[Redirect]] = IO.pure(None)

    def put(id: String, r: Redirect): IO[Either[Redirects.Error, Redirect]] =
      IO.pure(Left(AlreadyExists))
  }

}
