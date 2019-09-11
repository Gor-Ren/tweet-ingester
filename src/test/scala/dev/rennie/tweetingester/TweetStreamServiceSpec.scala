package dev.rennie.tweetingester

import cats.effect.{ContextShift, IO}
import org.http4s.Credentials.AuthParams
import org.http4s.headers.Authorization
import org.http4s.{Request, Uri}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.ExecutionContext

class TweetStreamServiceSpec
    extends FunSpec
    with Matchers
    with MockFactory
    with ScalaCheckPropertyChecks {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  val testCreds = TwitterApiCredentials("abc1", "def2", "ghi3", "klm4")
  val testReq: Request[IO] = Request[IO](uri = Uri.uri("/test/"))

  val mockClientBuilder: StreamingClientBuilder[IO] =
    mock[StreamingClientBuilder[IO]]

  val service =
    new TweetStreamService[IO](Uri.uri("/test/"), mockClientBuilder, testCreds)

  describe("TweetIngester#createTwitterStream") {
    it("should return an empty stream if there are no tweets") {
      (mockClientBuilder.streamClient _)
        .expects()
        .returns(
          MockClient[IO].returnsOkWith(Seq.empty)
        )

      val result = service.stream().compile.toVector.unsafeRunSync()
      result shouldBe empty
    }

    it("should return tweets received in the response") {
      val expected = Seq(Tweet(1L, "hello world"))
      (mockClientBuilder.streamClient _)
        .expects()
        .returns(
          MockClient[IO].returnsOkWith(expected)
        )

      val result = service.stream().compile.toVector.unsafeRunSync()
      result should contain allElementsOf expected
    }
  }

  describe("TweetIngester#sign") {
    it("should add OAuth headers to the request") {
      assume(testReq.headers.get(Authorization).isEmpty)
      val headers = service.sign(testReq)(testCreds).unsafeRunSync().headers
      headers.get(Authorization) shouldBe defined
    }

    it("should add the correct consumer key and token to the headers") {
      val headers = service.sign(testReq)(testCreds).unsafeRunSync().headers
      val authParams = headers
        .get(Authorization)
        .get
        .credentials
        .asInstanceOf[AuthParams]
        .params

      authParams.toList should contain allOf (
        ("oauth_consumer_key", testCreds.consumerKey),
        ("oauth_token", testCreds.accessToken)
      )
    }
  }
}
