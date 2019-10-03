package dev.rennie.tweetingester

import fs2.Stream
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

/** Builders which produce a singleton stream containing a [[Client]].
  *
  * Used to hide [[BlazeClientBuilder]] behind a trait so it can be
  * mocked in testing.
  */
trait StreamingClientBuilder[F[_]] {
  def streamClient: Stream[F, Client[F]]
}

object StreamingClientBuilder {

  /** Converts a [[BlazeClientBuilder]] to a [[StreamingClientBuilder]]. */
  implicit class BlazeClientStreamingBuilder[F[_]](b: BlazeClientBuilder[F])
      extends StreamingClientBuilder[F] {
    override def streamClient: Stream[F, Client[F]] = b.stream
  }
}
