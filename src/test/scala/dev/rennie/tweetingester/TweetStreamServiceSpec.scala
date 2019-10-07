package dev.rennie.tweetingester

import cats.effect.{ContextShift, IO}
import dev.rennie.tweetingester.mock.{
  CrlfDelimitedJsonEncoderInstances,
  MockTwitterClient
}
import fs2.Stream
import org.http4s.Credentials.AuthParams
import org.http4s.headers.Authorization
import org.http4s.{EntityEncoder, Request, Uri}
import org.scalacheck.Arbitrary
import org.scalamock.scalatest.MockFactory

import scala.concurrent.ExecutionContext

final class TweetStreamServiceSpec extends BaseTestSpec with MockFactory {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val twitterEncoder: EntityEncoder[IO, Stream[IO, Seq[Tweet]]] =
    CrlfDelimitedJsonEncoderInstances.tweetDelimitedJsonEncoder[IO]
  implicit val tweetArbitrary: Arbitrary[Tweet] = Arbitrary(tweetGen)

  val testConfig = TwitterConfig(
    "/test/",
    TwitterApiCredentials("abc1", "def2", "ghi3", "klm4")
  )

  val mockClientBuilder: StreamingClientBuilder[IO] =
    mock[StreamingClientBuilder[IO]]

  val service =
    new TweetStreamService[IO](testConfig, mockClientBuilder)

  describe("TweetIngester#stream") {
    it("should return tweets received in the response") {
      forAll { ts: Seq[Tweet] =>
        (mockClientBuilder.streamClient _)
          .expects()
          .returns(
            MockTwitterClient[IO](emitKeepAlive = false).returnsOkWith(ts)
          )

        val result = service.stream().compile.toVector.unsafeRunSync()
        result should contain allElementsOf ts
      }
    }

    it("should return tweets received with keep-alive signals") {
      forAll { ts: Seq[Tweet] =>
        (mockClientBuilder.streamClient _)
          .expects()
          .returns(
            MockTwitterClient[IO](emitKeepAlive = true).returnsOkWith(ts)
          )

        val result = service.stream().compile.toVector.unsafeRunSync()
        result should contain allElementsOf ts
      }
    }
  }

  describe("TweetIngester#sign") {
    val testReq: Request[IO] = Request[IO](uri = Uri.uri("/test/"))

    it("should add OAuth headers to the request") {
      assume(testReq.headers.get(Authorization).isEmpty)
      val headers =
        service.sign(testReq)(testConfig.credentials).unsafeRunSync().headers
      headers.get(Authorization) shouldBe defined
    }

    it("should add the correct consumer key and token to the headers") {
      val headers =
        service.sign(testReq)(testConfig.credentials).unsafeRunSync().headers
      val authParams = headers
        .get(Authorization)
        .get
        .credentials
        .asInstanceOf[AuthParams]
        .params

      authParams.toList should contain allOf (
        ("oauth_consumer_key", testConfig.credentials.consumerKey),
        ("oauth_token", testConfig.credentials.accessToken)
      )
    }
  }
}
