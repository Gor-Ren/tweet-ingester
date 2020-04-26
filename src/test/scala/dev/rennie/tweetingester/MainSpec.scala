package dev.rennie.tweetingester

import cats.Eq
import cats.effect.ExitCode
import dev.rennie.tweetingester.config.AppConfig
import dev.rennie.tweetingester.test.BaseTestSpec
import org.http4s.Uri
import org.scalatest.BeforeAndAfterAll
import cats.implicits.{catsSyntaxEq => _, _}

class MainSpec extends BaseTestSpec with BeforeAndAfterAll {

  implicit val exitCodeEq: Eq[ExitCode] = Eq.by(_.code)

  describe("TweetIngester#run") {
    it("should return a successful exit code") {
      assert(Main.run(List.empty).unsafeRunSync() === ExitCode.Success)
    }
  }

  describe("TweetIngester#loadConfig") {
    it("should load the expected config values") {

      val config: AppConfig = Main.loadConfig.unsafeRunSync()
      assert(config.twitter.sampleEndpoint === Uri.unsafeFromString("/test/"))
      assert(config.twitter.credentials.consumerKey.value === "test-consumer-key")
      assert(config.twitter.credentials.consumerSecret.value === "test-consumer-secret")
      assert(config.twitter.credentials.accessToken.value === "test-access-token")
      assert(config.twitter.credentials.accessSecret.value === "test-access-secret")
    }
  }
}
