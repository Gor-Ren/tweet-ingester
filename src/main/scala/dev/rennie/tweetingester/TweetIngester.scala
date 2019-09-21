package dev.rennie.tweetingester

import cats.effect.{ExitCode, IO, IOApp}
import javax.naming.ConfigurationException
import org.http4s.client.blaze.BlazeClientBuilder
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext

/** Application entry point for the Tweet Ingester. */
object TweetIngester extends IOApp {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      config <- loadConfig()
      service = new TweetStreamService[IO](
        config.twitter,
        BlazeClientBuilder[IO](ec)
      )
    } yield ExitCode.Success
  }

  /** Loads the application config in an effect.
    *
    * Config resolution will search in the resources directory.
    */
  def loadConfig(): IO[Config] = {
    IO.fromEither[Config](
      ConfigSource.default
        .load[Config]
        .left
        .map(f => new ConfigurationException(f.toString))
    )
  }
}
