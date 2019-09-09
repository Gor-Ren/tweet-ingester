package dev.rennie.tweetingester

import org.scalatest.{FunSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

final class TwitterApiCredentialsSpec
    extends FunSpec
    with Matchers
    with ScalaCheckPropertyChecks {

  describe("Twitter API Credentials") {
    it("should return the correct consumer credentials") {
      forAll { (cKey: String, cSecret: String, x: String, y: String) =>
        val creds = TwitterApiCredentials(cKey, cSecret, x, y)
        creds.oauthConsumer.key shouldBe cKey
        creds.oauthConsumer.secret shouldBe cSecret
      }
    }

    it("should return the correct token credentials") {
      forAll { (x: String, y: String, tValue: String, tSecret: String) =>
        val creds = TwitterApiCredentials(x, y, tValue, tSecret)
        creds.oauthToken.value shouldBe tValue
        creds.oauthToken.secret shouldBe tSecret
      }
    }
  }
}
