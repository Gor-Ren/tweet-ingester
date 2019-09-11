package dev.rennie.tweetingester

import org.http4s.{HttpApp, Request, Response}
import org.http4s.client.Client
import org.http4s.circe._
import io.circe.syntax._
import Tweet.tweetEncoder
import cats.effect.Sync
import fs2.Stream

class MockClient[F[_]](implicit syncf: Sync[F]) {

  def returnsOkWith(tweets: Seq[Tweet]): Stream[F, Client[F]] = {
    val route: Request[F] => F[Response[F]] =
      (_: Request[F]) =>
        syncf.delay(
          Response[F]().withEntity(tweets.asJson)
        )

    Stream.emit(
      Client.fromHttpApp[F](HttpApp(route))
    )
  }
}

object MockClient {
  def apply[F[_]](implicit syncf: Sync[F]) = new MockClient[F]()
}
