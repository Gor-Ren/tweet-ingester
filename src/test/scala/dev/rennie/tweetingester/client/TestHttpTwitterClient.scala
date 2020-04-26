package dev.rennie.tweetingester.client

import cats.Applicative
import cats.effect.Sync
import dev.rennie.tweetingester.domain.Tweet
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{EntityEncoder, HttpApp, Request, Response, Status}

object TestHttpTwitterClient {

  def returnsOkWith[F[_]: Sync](tweets: Seq[Tweet], emitKeepAlive: Boolean): Client[F] = {
    val enc =
      if (emitKeepAlive)
        CrlfDelimitedJsonEncoderInstances.tweetDelimitedKeepAliveJsonEncoder
      else
        CrlfDelimitedJsonEncoderInstances.tweetDelimitedJsonEncoder

    val route: Request[F] => F[Response[F]] =
      _ =>
        Sync[F].delay(
          Response[F](Status.Ok).withEntity(Stream.emit(tweets).covary[F])(enc)
        )

    Client.fromHttpApp[F](HttpApp(route))
  }

  def returnEmptyWithStatus[F[_]: Sync](status: Status): Client[F] = {
    Client.fromHttpApp[F](HttpApp { _ => Applicative[F].pure(Response[F](status = status)) })
  }
}
