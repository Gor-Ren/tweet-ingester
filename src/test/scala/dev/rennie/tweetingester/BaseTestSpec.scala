package dev.rennie.tweetingester

import dev.rennie.tweetingester.Tweet.Id
import eu.timepit.refined.api.Validate
import eu.timepit.refined.refineV
import eu.timepit.refined.string.ValidLong
import org.scalacheck.Gen
import org.scalatest.{FunSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class BaseTestSpec extends FunSpec with Matchers with ScalaCheckPropertyChecks {
  val tweetMaxLength = 140

  implicit val testy: Validate[String, ValidLong] = ValidLong.validLongValidate

  val idGen: Gen[Id] = for {
    long <- Gen.choose(1000L, Long.MaxValue)
    id = refineV[ValidLong](long.toString).getOrElse(throw new RuntimeException)
  } yield Id(id)

  val tweetGen: Gen[Tweet] = for {
    id <- idGen
    text <- Gen.asciiPrintableStr.suchThat(_.length < tweetMaxLength)
  } yield Tweet(id, text)
}
