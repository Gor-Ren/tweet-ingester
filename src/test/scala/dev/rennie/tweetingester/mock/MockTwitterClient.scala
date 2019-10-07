package dev.rennie.tweetingester.mock

import cats.effect.Sync
import dev.rennie.tweetingester.Tweet
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{EntityEncoder, HttpApp, Request, Response}

/** Creates HTTP clients which mock the Twitter API's tweet stream. */
class MockTwitterClient[F[_]](val enc: EntityEncoder[F, Stream[F, Seq[Tweet]]])(
    implicit F: Sync[F]
) {

  /** Returns a client which responds to every request with the input tweets. */
  def returnsOkWith(tweets: Seq[Tweet]): Stream[F, Client[F]] = {
    val route: Request[F] => F[Response[F]] =
      _ =>
        F.delay(
          Response[F]().withEntity(Stream.emit(tweets).covary[F])(enc)
        )

    Stream.emit(
      Client.fromHttpApp[F](HttpApp(route))
    )
  }
}

object MockTwitterClient {

  /**
    * Summons a [[MockTwitterClient]] for effect type `F`.
    *
    * @param emitKeepAlive if true, the client emits delimiters `\r\n` and keep
    *                      alive `\n` chars between tweet elements, else it only
    *                      emits the delimiters `\r\n`.
    * @return a client which mocks the Twitter API behaviour
    */
  def apply[F[_]](
      emitKeepAlive: Boolean
  )(implicit F: Sync[F]): MockTwitterClient[F] = {
    val enc =
      if (emitKeepAlive)
        CrlfDelimitedJsonEncoderInstances.tweetDelimitedKeepAliveJsonEncoder
      else
        CrlfDelimitedJsonEncoderInstances.tweetDelimitedJsonEncoder

    new MockTwitterClient[F](enc)
  }
}
