package dev.rennie.tweetingester

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.Uri
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

/** Application entry point for the Tweet Ingester. */
object TweetIngester extends IOApp {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  override def run(args: List[String]): IO[ExitCode] = {
    val service = new TweetStreamService[IO](
      Uri.uri(""),
      BlazeClientBuilder[IO](ec),
      TwitterApiCredentials("", "", "", "")
    )

    IO(ExitCode.Success)
  }
}
