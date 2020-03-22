package dev.rennie.tweetingester.mock

import cats.effect.IO
import dev.rennie.tweetingester.{BaseTestSpec, Tweet}
import fs2.Stream
import io.circe.syntax._
import org.http4s.{Request, Status}
import org.scalacheck.Arbitrary
import io.estatico.newtype.macros._
import org.http4s.client.Client

/** Tests that the mock client provides the expected behaviour. */
final class MockTwitterClientSpec extends BaseTestSpec {
  import MockTwitterClientSpec._
  implicit val tweetArbitrary: Arbitrary[Tweet] = Arbitrary(tweetGen)
  implicit val statusArb: Arbitrary[Status] = Arbitrary(statusGen)

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
        val (_, body) = doRequest(
          client.returnsOkWith(tweets),
          Request[IO]()
        )

        val expected =
          tweets.foldLeft("")((res, el) => res ++ el.asJson.spaces4 ++ "\r\n")

        body.value should equal(expected)
      }
    }

    it(
      "should respond with tweets as JSON separated by CRLF with keep-alive signal"
    ) {
      // init a client that encodes with the keep-alive signal
      val keepAliveClient =
        MockTwitterClient[IO](emitKeepAlive = true)

      forAll { tweets: Seq[Tweet] =>
        val (_, body) = doRequest(
          keepAliveClient.returnsOkWith(tweets),
          Request[IO]()
        )

        val expected =
          tweets.foldLeft("")(
            (res, el) => res ++ el.asJson.spaces4 ++ "\r\n" ++ "\n"
          )

        body.value should equal(expected)
      }
    }
  }

  describe("MockTwitterClient#returnEmptyWithStatus") {
    it("should return a response with the input status and an empty body") {
      forAll { status: Status =>
        val (actualStatus, body) = doRequest(
          client.returnEmptyWithStatus(status),
          Request[IO]()
        )

        body.value shouldBe empty
        actualStatus shouldBe status
      }
    }
  }
}

object MockTwitterClientSpec {
  @newtype case class Body(value: String)

  def doRequest(
      client: Stream[IO, Client[IO]],
      request: Request[IO]
  ): (Status, Body) =
    client
      .evalMap(
        _.fetch(request)(
          resp =>
            resp.bodyAsText.compile.string.map { str =>
              (resp.status, Body(str))
            }
        )
      )
      .compile
      .toList
      .unsafeRunSync()
      .headOption
      .get
}
