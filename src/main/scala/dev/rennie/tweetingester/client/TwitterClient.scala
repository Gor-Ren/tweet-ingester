package dev.rennie.tweetingester.client

import cats.effect.{ConcurrentEffect, ContextShift, Resource}
import cats.syntax.monadError._
import dev.rennie.tweetingester.domain.Tweet.tweetDecoder
import dev.rennie.tweetingester.config.{TwitterApiCredentials, TwitterConfig}
import dev.rennie.tweetingester.domain.{ConnectionFailure, Tweet}
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.circe.jawn.CirceSupportParser
import io.circe.{Decoder, Json}
import jawnfs2._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.{oauth1, Client}
import org.http4s.{Method, Request, Response}
import org.typelevel.jawn.Facade
import eu.timepit.refined.auto._

import scala.concurrent.ExecutionContext.global

trait TwitterClient[F[_]] {
  def sampleTweets: Stream[F, Tweet]
}

final class LiveTwitterClient[F[_]: ConcurrentEffect: ContextShift: Logger](
    config: TwitterConfig,
    httpClient: Resource[F, Client[F]]
) extends TwitterClient[F] {

  implicit val circeFacade: Facade[Json] = CirceSupportParser.facade

  def sampleTweets: Stream[F, Tweet] = {
    val request = Request[F](Method.GET, config.sampleEndpoint)
    val response = connect(request)(config.credentials)
    response
      .ensure(
        ConnectionFailure("Did not receive success code from Twitter")
      )(r => r.status.isSuccess)
      .flatMap(_.body.chunks)
      .through(parseJsonStream)
      .map(Decoder[Tweet].decodeJson)
      .flatMap(
        _.fold(
          failure =>
            Stream.eval(
              Logger[F].warn(s"Tweet decode failure: ${failure.message}")
            ) >> Stream.empty,
          tweet => Stream.emit(tweet)
        )
      )
  }

  private[client] def connect(
      req: Request[F]
  )(creds: TwitterApiCredentials): Stream[F, Response[F]] =
    for {
      client <- Stream.resource(httpClient)
      _ <- Stream.eval(
        Logger[F].info(s"Connecting to stream tweets from ${req.uri}")
      )
      signedRequest: Request[F] <- Stream.eval(sign(req, creds))
      // TODO: response error handling / retry logic
      response: Response[F] <- client.stream(signedRequest)
    } yield response

  // TODO: upgrade to oauth2
  private[client] def sign(
      req: Request[F],
      creds: TwitterApiCredentials
  ): F[Request[F]] = {
    oauth1.signRequest(
      req,
      creds.oauthConsumer,
      callback = None,
      verifier = None,
      token = Some(creds.oauthToken)
    )
  }
}

object LiveTwitterClient {

  def make[F[_]: ConcurrentEffect: ContextShift: Logger](
      config: TwitterConfig
  ): TwitterClient[F] =
    new LiveTwitterClient[F](config, BlazeClientBuilder[F](global).resource)
}
