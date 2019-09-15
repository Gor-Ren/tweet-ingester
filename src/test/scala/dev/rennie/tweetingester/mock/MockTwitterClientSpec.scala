package dev.rennie.tweetingester.mock

import cats.effect.IO
import dev.rennie.tweetingester.Tweet
import org.scalatest.{FunSpec, Matchers}
import dev.rennie.tweetingester.mock.CrlfDelimitedJsonEncoderInstances.tweetDelimitedJsonEncoder
import org.http4s.Request
import org.http4s.circe._
import io.circe.syntax._

final class MockTwitterClientSpec extends FunSpec with Matchers {
  val client = new MockTwitterClient[IO](tweetDelimitedJsonEncoder[IO])

  describe("MockTwitterClient#returnsOkWith") {
    it("should return a singleton stream") {
      val c = client.returnsOkWith(Seq.empty).compile.toList.unsafeRunSync()
      c.size shouldBe 1
    }

    it("should return an OK (200) response") {
      val c =
        client.returnsOkWith(Seq.empty).compile.toList.unsafeRunSync().head
      c.successful(Request[IO]()).unsafeRunSync() shouldBe true
    }

    it("should respond with tweets as JSON separated by CRLF") {
      val tweets = Seq(Tweet(1, "1"), Tweet(2, "2"))
      val c =
        client.returnsOkWith(tweets).compile.toList.unsafeRunSync().head
      val body = c
        .fetch(Request[IO]())(resp => resp.bodyAsText.compile.string)
        .unsafeRunSync()

      val expected =
        tweets.foldLeft("")((res, el) => res ++ el.asJson.noSpaces ++ "\r\n")

      body should equal(expected)
    }
  }
}
