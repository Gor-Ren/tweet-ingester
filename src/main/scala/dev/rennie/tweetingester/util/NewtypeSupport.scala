package dev.rennie.tweetingester.util
import cats.{Contravariant, Eq, Show}
import cats.instances.eq._
import cats.syntax.contravariant._
import cats.instances.string._
import cats.instances.int._
import eu.timepit.refined.api.RefType
import eu.timepit.refined.cats.derivation._
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

    implicit def coercibleRefinedEq[F[_, _]: RefType, A: Eq, P, B: Coercible[F[A, P], *]]: Eq[B] = {
      coercibleEq[F[A, P], B]
    }

    implicit def coercibleStringEq[B: Coercible[String, *]]: Eq[B] =
      coercibleEq[String, B]

    implicit def coercibleIntEq[B: Coercible[Int, *]]: Eq[B] =
      coercibleEq[Int, B]

    private def coercibleShow[A: Show, B: Coercible[A, *]]: Show[B] =
      new Show[B] {
        override def show(t: B): String = Show[A].show(t.repr.asInstanceOf[A])
      }

    implicit def coercibleStringShow[B: Coercible[String, *]]: Show[B] =
      coercibleShow[String, B]

    implicit def coercibleIntShow[B: Coercible[Int, *]]: Show[B] =
      coercibleShow[Int, B]

    implicit def coercibleRefinedShow[F[_, _]: RefType, A: Show, P, B: Coercible[F[A, P], *]]
        : Show[B] = {
      implicit val fShow: Show[F[A, P]] = refTypeViaContravariant[F, Show, A, P]
      coercibleShow[F[A, P], B]
    }
  }
}
