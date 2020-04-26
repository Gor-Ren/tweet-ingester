package dev.rennie.tweetingester.client

import cats.effect.{ContextShift, IO, Resource}
import cats.instances.either._
import cats.instances.string._
import cats.instances.list._
import cats.syntax.show._
import dev.rennie.tweetingester.test.{BaseTestSpec, TweetGenerator}
import dev.rennie.tweetingester.config.{TwitterApiCredentials, TwitterConfig}
import dev.rennie.tweetingester.domain.{ConnectionFailure, Tweet}
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.Credentials.AuthParams
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.{EntityEncoder, Request, Status, Uri}
import org.scalacheck.Arbitrary
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import eu.timepit.refined.auto._

import scala.concurrent.ExecutionContext

final class LiveTwitterClientSpec extends BaseTestSpec with MockFactory with EitherValues {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val log: Logger[IO] = Slf4jLogger.unsafeCreate[IO]
  implicit val twitterEncoder: EntityEncoder[IO, Stream[IO, Seq[Tweet]]] =
    CrlfDelimitedJsonEncoderInstances.tweetDelimitedJsonEncoder[IO]
  implicit val tweetArbitrary: Arbitrary[Tweet] = Arbitrary(TweetGenerator.tweetGen)

  val testConfig = TwitterConfig(
    Uri.unsafeFromString("/test/"),
    TwitterApiCredentials("abc1", "def2", "ghi3", "klm4")
  )

  def twitterClient(httpClient: Client[IO]) =
    new LiveTwitterClient[IO](testConfig, Resource.pure[IO, Client[IO]](httpClient))

  describe("TweetIngester#sampleTweets") {
    it("should raise a ConnectionFailure if the Twitter API returns an error") {
      val httpClient =
        TestHttpTwitterClient.returnEmptyWithStatus[IO](Status.BadRequest)

      val result =
        twitterClient(httpClient).sampleTweets.compile.toList.attempt
          .unsafeRunSync()

      result.fold(
        err => assert(err.isInstanceOf[ConnectionFailure]),
        success => fail(s"expected error, but was: ${success.show}")
      )
    }

    it("should return tweets received in the response") {
      forAll { ts: List[Tweet] =>
        val httpClient =
          TestHttpTwitterClient.returnsOkWith[IO](ts, emitKeepAlive = false)

        val result = twitterClient(httpClient).sampleTweets.compile.toList
          .unsafeRunSync()

        assert(result === ts)
      }
    }

    it("should return tweets received with keep-alive signals") {
      forAll { ts: List[Tweet] =>
        val httpClient =
          TestHttpTwitterClient.returnsOkWith[IO](ts, emitKeepAlive = true)

        val result = twitterClient(httpClient).sampleTweets.compile.toList
          .unsafeRunSync()

        assert(result === ts)
      }
    }
  }

  describe("TweetIngester#sign") {
    val testReq: Request[IO] = Request[IO](uri = Uri.uri("/test/"))
    val client = twitterClient(
      TestHttpTwitterClient.returnEmptyWithStatus[IO](Status.BadRequest)
    )

    it("should add OAuth headers to the request") {
      assume(testReq.headers.get(Authorization).isEmpty)
      val headers =
        client
          .sign(testReq, testConfig.credentials)
          .unsafeRunSync()
          .headers
      headers.get(Authorization) shouldBe defined
    }

    it("should add the correct consumer key and token to the headers") {
      val headers =
        client
          .sign(testReq, testConfig.credentials)
          .unsafeRunSync()
          .headers
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

object LiveTwitterClientSpec {
  // TODO: add helper method to make client
}
