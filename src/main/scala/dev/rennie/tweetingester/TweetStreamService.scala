package dev.rennie.tweetingester

import cats.effect.{ConcurrentEffect, ContextShift, IO}
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
    config: TwitterConfig,
    clientBuilder: StreamingClientBuilder[F]
)(implicit ec: ExecutionContext, F: ConcurrentEffect[F], cs: ContextShift[F]) {

  implicit val circeFacade: RawFacade[Json] = CirceSupportParser.facade

  /** Connects to the Twitter endpoint and returns stream of parsed [[Tweet]]s.
    *
    * Elements which cannot be parsed to a [[Tweet]] are ignored.
    */
  def stream(): Stream[F, Tweet] = {
    val request = Request[F](Method.GET, config.endpointUri)
    val response = createTwitterStream(request)(config.credentials)
    for {
      r <- response
      tweet <- r.body.chunks
        .through(parseJsonStream)
        .through(decoder[F, Tweet])
        .handleErrorWith(t => {
          Stream.eval(IO(println(s"Error: $t"))) // TODO: log errors
          Stream.empty
        })
    } yield tweet
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
