package dev.rennie.tweetingester.test

import cats.effect.IO
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.testing.TestingLogger
import org.scalatest.{FunSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

trait BaseTestSpec extends FunSpec with Matchers with ScalaCheckPropertyChecks with CatsEquality {
  implicit val logger: Logger[IO] = TestingLogger.impl[IO]()
}
