package dev.chrs.urlshortener

import scala.concurrent.ExecutionContext.global
import scala.io.Source

import cats.effect._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.blaze._

object URLShortener extends IOApp {

  import org.http4s.dsl.io._

  def heartbeat: HttpRoutes[IO] = HttpRoutes
    .of[IO] { case GET -> Root / "heartbeat" =>
      Ok("i'm alive")
    }

  def run(args: List[String]): IO[ExitCode] = {
    for {
      adjs <- IO(Source.fromResource("adjs.txt")).map(_.getLines().toList)
      nouns <- IO(Source.fromResource("nouns.txt")).map(_.getLines().toList)

      rr = RedirectRepository[IO]
      haiku = Haiku[IO](adjs, nouns, 1000 to 9999)
      service = RedirectService[IO](rr, haiku)

      exitCode <- BlazeServerBuilder[IO](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(heartbeat.orNotFound)
        .withHttpApp(service.routes.orNotFound)
        .withSocketKeepAlive(true)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    } yield exitCode
  }

}
