package dev.rennie.tweetingester.domain

import cats.{Eq, Show}
import cats.instances.string._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString

import scala.util.control.NoStackTrace

sealed trait ClientError extends NoStackTrace

object ClientError {
  implicit val clientErrorEq: Eq[ClientError] = {
    import cats.derived.auto.eq._
    cats.derived.semi.eq
  }

  implicit val clientErrorShow: Show[ClientError] = {
    import cats.derived.auto.show._
    cats.derived.semi.show
  }
}

final case class ConnectionFailure(message: NonEmptyString) extends ClientError

object ConnectionFailure {
  implicit val connectionFailureEq: Eq[ConnectionFailure] = {
    import cats.derived.auto.eq._
    cats.derived.semi.eq
  }

  implicit val connectionFailureShow: Show[ConnectionFailure] = {
    import cats.derived.auto.show._
    cats.derived.semi.show
  }
}
