package dev.rennie.tweetingester

import org.scalacheck.Gen
import org.scalatest.{FunSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class BaseTestSpec extends FunSpec with Matchers with ScalaCheckPropertyChecks {
  val tweetMaxLength = 140

  val tweetGen: Gen[Tweet] = for {
    id <- Gen.chooseNum(Long.MinValue, Long.MaxValue)
    text <- Gen.asciiPrintableStr.suchThat(_.length < tweetMaxLength)
  } yield Tweet(id, text)
}
