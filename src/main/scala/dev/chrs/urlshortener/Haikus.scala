package dev.chrs.urlshortener

import scala.util.Random.nextInt

import cats.effect._
import cats.implicits._

trait Haikus[F[_]] {
  def get(): F[String]
}

object LiveHaikus {
  def make[F[_]: Effect](adjs: List[String], nouns: List[String], ra: Range): F[LiveHaikus[F]] =
    Sync[F].delay(new LiveHaikus(adjs, nouns, ra))
}

/**
 * Haiku generation based on https://github.com/bmarcot/haiku.
 *
 * Haikus follow the pattern:
 *
 * {{{
 *   adj-noun-0000
 * }}}
 *
 * @param adjs List of adjectives to use.
 * @param nouns List of nouns to use.
 * @param ra Range of numbers to use.
 * @param F
 */
class LiveHaikus[F[_]: Effect] private (adjs: List[String], nouns: List[String], ra: Range)
    extends Haikus[F] {

  private def randomElement(xs: List[String]): F[String] =
    Effect[F].pure(nextInt(xs.size)).map(xs(_))

  private def randomNumber(ra: Range): F[String] =
    Effect[F].pure(nextInt(ra.end - ra.head)).map(random => (ra.head + random).toString)

  /**
   * Create a random haiku.
   *
   * @return A random haiku.
   */
  def get(): F[String] = {
    val xs = randomNumber(ra) :: List(nouns, adjs).map(randomElement)
    xs.sequence.map(_.reverse.mkString("-"))
  }
}
