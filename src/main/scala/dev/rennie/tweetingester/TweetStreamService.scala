package dev.rennie.tweetingester

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Sync}
import dev.rennie.tweetingester.Tweet.tweetDecoder
import fs2.Stream
import io.circe.jawn.CirceSupportParser
import io.circe.{Decoder, Json}
import jawnfs2._
import org.http4s.client.{oauth1, Client}
import org.http4s.{Method, Request, Response}
import org.typelevel.jawn.Facade

/**
  * Connects to the Twitter API and produces a stream of parsed [[Tweet]]s.
  *
  * @param config the Twitter API details used to connect
  * @param clientBuilder a builder producing a singleton stream of the HTTP
  *                      client to be used to communicate with Twitter
  */
class TweetStreamService[F[_]: ConcurrentEffect: ContextShift: Applicative](
    config: TwitterConfig,
    clientBuilder: StreamingClientBuilder[F]
) {

  implicit val circeFacade: Facade[Json] = CirceSupportParser.facade

  /** Connects to the Twitter endpoint and returns stream of parsed [[Tweet]]s.
    *
    * Elements which cannot be parsed to a [[Tweet]] are ignored.
    */
  def stream(): Stream[F, Tweet] = {
    val request = Request[F](Method.GET, config.endpointUri)
    val response = createTwitterStream(request)(config.credentials)
    response
      .flatMap(_.body.chunks)
      .through(parseJsonStream)
      .map(Decoder[Tweet].decodeJson)
      .flatMap(
        _.fold(failure =>
                 Stream.eval(Sync[F].delay { // TODO: logging
                   println(s"Tweet decode error: $failure")
                 }) >> Stream.empty,
               tweet => Stream.emit(tweet))
      )
  }

  /** Executes the input request and returns the streamed response.
    *
    * @param req the request to be sent
    * @param creds the API credentials to sign the request with
    * @return the response wrapped in a stream
    */
  def createTwitterStream(
      req: Request[F]
  )(creds: TwitterApiCredentials): Stream[F, Response[F]] =
    for {
      client: Client[F] <- clientBuilder.streamClient
      signedRequest: Request[F] <- Stream.eval(sign(req)(creds))
      // TODO: response error handling / retry logic
      response: Response[F] <- client.stream(signedRequest)
    } yield response

  /** Signs the request with the input credentials.
    *
    * The returned request is wrapped in an effect due to the creation of a
    * nonce with the system clock during OAuth signing.
    *
    * @param req the request to be signed
    * @param creds Twitter API credentials to use for signing
    * @return the signed request wrapped in an effect
    */
  def sign(req: Request[F])(creds: TwitterApiCredentials): F[Request[F]] = {
    oauth1.signRequest(req,
                       creds.oauthConsumer,
                       callback = None,
                       verifier = None,
                       token = Some(creds.oauthToken))
  }
}
