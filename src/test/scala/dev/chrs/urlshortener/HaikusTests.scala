package dev.chrs.urlshortener

import cats.effect._
import cats.effect.testing.utest._
import utest._

object HaikusTests extends IOTestSuite {

  override def tests: Tests = Tests {
    test("a simple haiku should be created from adjs, nouns and range") {
      for {
        haikus <- LiveHaikus.make[IO](List("adj"), List("noun"), 1 until 2)
        result <- haikus.get()
      } yield assert(result == "adj-noun-1")
    }
  }

}
