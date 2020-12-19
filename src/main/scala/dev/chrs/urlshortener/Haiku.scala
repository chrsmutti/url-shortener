package dev.chrs.urlshortener

import scala.util.Random.nextInt

import cats.effect._
import cats.implicits._

object Haiku {
  def apply[F[_]](adjs: List[String], nouns: List[String], ra: Range)(implicit F: Effect[F]) =
    new HaikuImpl[F](adjs, nouns, ra)
}

trait Haiku[F[_]] {
  def get(): F[String]
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
class HaikuImpl[F[_]](adjs: List[String], nouns: List[String], ra: Range)(implicit F: Effect[F])
    extends Haiku[F] {

  private def randomElement(xs: List[String]): F[String] =
    F.pure(nextInt(xs.size)).map(xs(_))

  private def randomNumber(ra: Range): F[String] =
    F.pure(nextInt(ra.end - ra.head)).map(random => (ra.head + random).toString)

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
