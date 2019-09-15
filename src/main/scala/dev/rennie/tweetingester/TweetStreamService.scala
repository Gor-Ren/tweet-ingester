package dev.rennie.tweetingester

import cats.effect.{ConcurrentEffect, ContextShift}
import io.circe.Json
import org.http4s.{Method, Request, Response, Uri}
import org.http4s.client.{oauth1, Client}
import fs2.Stream
import jawnfs2._
import org.typelevel.jawn.RawFacade
import io.circe.jawn.CirceSupportParser
import Tweet.tweetDecoder
import io.circe.fs2.decoder

import scala.concurrent.ExecutionContext

class TweetStreamService[F[_]](
    twitterEndpoint: Uri,
    clientBuilder: StreamingClientBuilder[F],
    creds: TwitterApiCredentials
)(implicit ec: ExecutionContext, F: ConcurrentEffect[F], cs: ContextShift[F]) {

  implicit val circeFacade: RawFacade[Json] = CirceSupportParser.facade

  /** Connects to the Twitter endpoint and returns stream of parsed [[Tweet]]s.
    *
    * Elements which cannot be parsed to a [[Tweet]] are ignored.
    */
  def stream(): Stream[F, Tweet] = {
    val request = Request[F](Method.GET, twitterEndpoint)
    val response = createTwitterStream(request)(creds)
    for {
      r <- response
      tweets <- r.body.chunks
        .through(parseJsonStream)
        .through(decoder[F, Tweet])
        .handleErrorWith(t => {
          println(s"Error: $t") // TODO: log errors, make pure
          Stream.empty
        })
    } yield tweets
  }

  /** Obtains a streamed response using the input request and credentials. */
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
