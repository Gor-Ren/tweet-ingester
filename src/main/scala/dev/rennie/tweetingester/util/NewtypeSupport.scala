package dev.rennie.tweetingester.util
import cats.Eq
import cats.instances.string._
import cats.instances.int._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._

object NewtypeSupport {
  object NewtypeCats {
    // credit to Gabriel Volpe: https://github.com/gvolpe/pfps-shopping-cart/
    // does not work as implicit for some reason...
    private def coercibleEq[A: Eq, B: Coercible[A, *]]: Eq[B] =
      new Eq[B] {
        def eqv(x: B, y: B): Boolean =
          Eq[A].eqv(x.repr.asInstanceOf[A], y.repr.asInstanceOf[A])
      }

    implicit def coercibleStringEq[B: Coercible[String, *]]: Eq[B] =
      coercibleEq[String, B]

    implicit def coercibleIntEq[B: Coercible[Int, *]]: Eq[B] =
      coercibleEq[Int, B]
  }
}
