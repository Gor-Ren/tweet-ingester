package dev.rennie.tweetingester.test
import dev.rennie.tweetingester.config.TwitterApiCredentials
import eu.timepit.refined.types.string.NonEmptyString
import org.scalacheck.Gen

object ConfigGenerator {
  val genNesPrintableAscii: Gen[NonEmptyString] = Gen
    .nonEmptyListOf(Gen.asciiPrintableChar)
    .map(_.mkString)
    .map(NonEmptyString.unsafeFrom)

  val credGen: Gen[TwitterApiCredentials] = for {
    consumerKey <- genNesPrintableAscii
    consumerSecret <- genNesPrintableAscii
    tokenValue <- genNesPrintableAscii
    tokenSecret <- genNesPrintableAscii
  } yield TwitterApiCredentials(consumerKey, consumerSecret, tokenValue, tokenSecret)
}
