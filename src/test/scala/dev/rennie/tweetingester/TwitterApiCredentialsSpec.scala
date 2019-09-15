package dev.rennie.tweetingester

import org.scalacheck.{Arbitrary, Gen}

final class TwitterApiCredentialsSpec extends BaseTestSpec {

  val credGen: Gen[TwitterApiCredentials] = for {
    consumerKey <- Gen.asciiPrintableStr
    consumerSecret <- Gen.asciiPrintableStr
    tokenValue <- Gen.asciiPrintableStr
    tokenSecret <- Gen.asciiPrintableStr
  } yield TwitterApiCredentials(consumerKey,
                                consumerSecret,
                                tokenValue,
                                tokenSecret)

  implicit val credArbitrary: Arbitrary[TwitterApiCredentials] =
    Arbitrary(credGen)

  describe("Twitter API Credentials") {
    it("should return the correct consumer credentials") {
      forAll { creds: TwitterApiCredentials =>
        creds.oauthConsumer.key shouldBe creds.consumerKey
        creds.oauthConsumer.secret shouldBe creds.consumerSecret
      }
    }

    it("should return the correct token credentials") {
      forAll { creds: TwitterApiCredentials =>
        creds.oauthToken.value shouldBe creds.accessToken
        creds.oauthToken.secret shouldBe creds.accessSecret
      }
    }
  }
}
