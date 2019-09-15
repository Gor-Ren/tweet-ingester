package dev.rennie.tweetingester

import cats.effect.ExitCode

class TweetIngesterSpec extends BaseTestSpec {
  describe("Tweet Ingester") {
    it("should return a successful exit code") {
      TweetIngester.run(List.empty).unsafeRunSync() shouldBe ExitCode.Success
    }
  }
}
