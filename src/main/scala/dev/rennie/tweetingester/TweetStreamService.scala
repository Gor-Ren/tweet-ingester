package dev.rennie.tweetingester

import cats.effect.{ConcurrentEffect, ContextShift}
import io.circe.Json
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request, Response, Uri}
import org.http4s.client.oauth1
import jawnfs2._
import org.typelevel.jawn.RawFacade
import io.circe.jawn.CirceSupportParser
import Tweet.decodeTweet

import scala.concurrent.ExecutionContext

class TweetStreamService[F[_]](
    twitterEndpoint: Uri,
    clientBuilder: BlazeClientBuilder[F],
    creds: TwitterApiCredentials
)(implicit ec: ExecutionContext, F: ConcurrentEffect[F], cs: ContextShift[F]) {

  implicit val circeFacade: RawFacade[Json] = CirceSupportParser.facade

  def stream(): fs2.Stream[F, Tweet] = {
    val req = Request[F](Method.GET, twitterEndpoint)
    parseTweets(createTwitterStream(req)(creds))
  }

  /** Transforms the input stream response to a stream of [[Tweet]].
    *
    * Malformed JSON elements which cannot be parsed are ignored.
    */
  def parseTweets(
      twitterStream: fs2.Stream[F, Response[F]]
  ): fs2.Stream[F, Tweet] =
    for {
      resp <- twitterStream
      tweetJson <- resp.body.chunks.parseJsonStream
      tweet <- tweetJson.as[Tweet] match {
        case Left(_)  => fs2.Stream.empty // TODO: log parse failures
        case Right(t) => fs2.Stream.emit(t)
      }
    } yield tweet

  /** Obtains a streamed response using the input request and credentials. */
  def createTwitterStream(
      req: Request[F]
  )(creds: TwitterApiCredentials): fs2.Stream[F, Response[F]] =
    for {
      client <- clientBuilder.stream
      signedRequest <- fs2.Stream.eval(sign(req)(creds))
      // TODO: response error handling / retry logic
      response <- client.stream(signedRequest)
    } yield response

  /** Signs the request with the input credentials.
    *
    * The returned request is wrapped in an effect due to the creation of a
    * nonce during OAuth signing.
    *
    * @param req the request to be signed
    * @param creds Twitter API credentials to use for signing
    * @return
    */
  def sign(req: Request[F])(creds: TwitterApiCredentials): F[Request[F]] = {
    oauth1.signRequest(req,
                       creds.oauthConsumer,
                       callback = None,
                       verifier = None,
                       token = Some(creds.oauthToken))
  }
}
