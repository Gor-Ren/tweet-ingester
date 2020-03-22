package dev.rennie.tweetingester

import cats.effect.{ExitCode, IO}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.scalatest.BeforeAndAfterAll

class MainSpec extends BaseTestSpec with BeforeAndAfterAll {
  describe("TweetIngester#run") {
    it("should return a successful exit code") {
      Main.run(List.empty).unsafeRunSync() shouldBe ExitCode.Success
    }
  }

  describe("TweetIngester#loadConfig") {
    it("should load the expected config values") {
      implicit val logger = Slf4jLogger.unsafeCreate[IO]
      val config: Config = Main.loadConfig.unsafeRunSync()
      config.twitter.endpoint shouldBe "/test/"
      config.twitter.credentials.consumerKey shouldBe "test-consumer-key"
      config.twitter.credentials.consumerSecret shouldBe "test-consumer-secret"
      config.twitter.credentials.accessToken shouldBe "test-access-token"
      config.twitter.credentials.accessSecret shouldBe "test-access-secret"
    }
  }
}
