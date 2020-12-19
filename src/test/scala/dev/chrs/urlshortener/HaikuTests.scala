package dev.chrs.urlshortener

import cats.effect._
import cats.effect.testing.utest._
import utest._

object HaikuTests extends IOTestSuite {

  private val haiku = new HaikuImpl[IO](List("adj"), List("noun"), 1 until 2)

  override def tests: Tests = Tests {
    test("a simple haiku should be created from adjs, nouns and range") {
      for {
        result <- haiku.get()
      } yield assert(result == "adj-noun-1")
    }
  }

}
