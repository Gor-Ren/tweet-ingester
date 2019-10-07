package dev.rennie.tweetingester.mock

import cats.effect.IO
import dev.rennie.tweetingester.{BaseTestSpec, Tweet}
import org.http4s.Request
import io.circe.syntax._
import org.scalacheck.Arbitrary

/** Tests that the mock client provides the expected behaviour. */
final class MockTwitterClientSpec extends BaseTestSpec {
  implicit val tweetArbitrary: Arbitrary[Tweet] = Arbitrary(tweetGen)

  val client: MockTwitterClient[IO] =
    MockTwitterClient[IO](emitKeepAlive = false)

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
      forAll { tweets: Seq[Tweet] =>
        val c =
          client.returnsOkWith(tweets).compile.toList.unsafeRunSync().head
        val body = c
          .fetch(Request[IO]())(resp => resp.bodyAsText.compile.string)
          .unsafeRunSync()

        val expected =
          tweets.foldLeft("")((res, el) => res ++ el.asJson.spaces4 ++ "\r\n")

        body should equal(expected)
      }
    }

    it(
      "should respond with tweets as JSON separated by CRLF with keep-alive signal"
    ) {
      // init a client that encodes with the keep-alive signal
      val keepAliveClient =
        MockTwitterClient[IO](emitKeepAlive = true)

      forAll { tweets: Seq[Tweet] =>
        val c = keepAliveClient
          .returnsOkWith(tweets)
          .compile
          .toList
          .unsafeRunSync()
          .head
        val body = c
          .fetch(Request[IO]())(resp => resp.bodyAsText.compile.string)
          .unsafeRunSync()

        val expected =
          tweets.foldLeft("")(
            (res, el) => res ++ el.asJson.spaces4 ++ "\r\n" ++ "\n"
          )

        body should equal(expected)
      }
    }
  }
}
