package dev.rennie.tweetingester

import cats.effect.{ContextShift, IO}
import dev.rennie.tweetingester.TwitterClient.ConnectionFailure
import dev.rennie.tweetingester.mock.{
  CrlfDelimitedJsonEncoderInstances,
  MockTwitterClient
}
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.Credentials.AuthParams
import org.http4s.headers.Authorization
import org.http4s.{EntityEncoder, Request, Status, Uri}
import org.scalacheck.Arbitrary
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues

import scala.concurrent.ExecutionContext

final class TwitterClientSpec
    extends BaseTestSpec
    with MockFactory
    with EitherValues {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val log: Logger[IO] = Slf4jLogger.unsafeCreate[IO]
  implicit val twitterEncoder: EntityEncoder[IO, Stream[IO, Seq[Tweet]]] =
    CrlfDelimitedJsonEncoderInstances.tweetDelimitedJsonEncoder[IO]
  implicit val tweetArbitrary: Arbitrary[Tweet] = Arbitrary(tweetGen)

  val testConfig = TwitterConfig(
    "/test/",
    TwitterApiCredentials("abc1", "def2", "ghi3", "klm4")
  )

  def client(clientBuilder: StreamingClientBuilder[IO]) =
    new TwitterClient[IO](testConfig, clientBuilder)

  describe("TweetIngester#stream") {
    it("should raise a ConnectionFailure if the Twitter API returns an error") {
      val builder = (mock[StreamingClientBuilder[IO]].streamClient _)
        .expects()
        .returns(
          MockTwitterClient[IO](emitKeepAlive = false)
            .returnEmptyWithStatus(Status.BadRequest)
        )

      val result =
        client(builder).stream.attempt.compile.drain.attempt.unsafeRunSync()
      println("*******************" + result)
      result.left.value shouldBe a[ConnectionFailure]
    }

    it("should return tweets received in the response") {
      forAll { ts: Seq[Tweet] =>
        val builder = (mock[StreamingClientBuilder[IO]].streamClient _)
          .expects()
          .returns(
            MockTwitterClient[IO](emitKeepAlive = false).returnsOkWith(ts)
          )

        val result = client(builder).stream.compile.toVector.unsafeRunSync()
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

        val result = client().stream.compile.toVector.unsafeRunSync()
        result should contain allElementsOf ts
      }
    }
  }

  describe("TweetIngester#sign") {
    val testReq: Request[IO] = Request[IO](uri = Uri.uri("/test/"))

    it("should add OAuth headers to the request") {
      assume(testReq.headers.get(Authorization).isEmpty)
      val headers =
        client.sign(testReq)(testConfig.credentials).unsafeRunSync().headers
      headers.get(Authorization) shouldBe defined
    }

    it("should add the correct consumer key and token to the headers") {
      val headers =
        client.sign(testReq)(testConfig.credentials).unsafeRunSync().headers
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

object TwitterClientSpec {
  // TODO: add helper method to make client
  // TODO: use implementations of the StreamingClientBuilder trait instead of scala mocks
}
