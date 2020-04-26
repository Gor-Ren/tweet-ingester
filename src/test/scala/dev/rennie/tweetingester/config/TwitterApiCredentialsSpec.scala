package dev.rennie.tweetingester.config

import cats.instances.string._
import dev.rennie.tweetingester.test.{BaseTestSpec, ConfigGenerator}
import org.scalacheck.Arbitrary

final class TwitterApiCredentialsSpec extends BaseTestSpec {

  implicit val credArbitrary: Arbitrary[TwitterApiCredentials] = Arbitrary(ConfigGenerator.credGen)

  describe("Twitter API Credentials") {
    it("should return the correct consumer credentials") {
      forAll { creds: TwitterApiCredentials =>
        assert(creds.oauthConsumer.key === creds.consumerKey.value)
        assert(creds.oauthConsumer.secret === creds.consumerSecret.value)
      }
    }

    it("should return the correct token credentials") {
      forAll { creds: TwitterApiCredentials =>
        assert(creds.oauthToken.value === creds.accessToken.value)
        assert(creds.oauthToken.secret === creds.accessSecret.value)
      }
    }
  }
}
