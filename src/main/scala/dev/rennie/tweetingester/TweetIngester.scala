package dev.rennie.tweetingester

import cats.effect.{ExitCode, IO, IOApp}

/** Application entry point for the Tweet Ingester. */
object TweetIngester extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = IO(ExitCode.Success)
}
