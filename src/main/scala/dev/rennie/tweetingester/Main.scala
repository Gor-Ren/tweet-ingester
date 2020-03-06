package dev.rennie.tweetingester

import java.nio.charset.StandardCharsets

import cats.effect.{ExitCode, IO, IOApp}
import fs2.kafka.{ProducerSettings, Serializer}
import io.circe.syntax._
import javax.naming.ConfigurationException
import org.http4s.client.blaze.BlazeClientBuilder
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext

/** Application entry point for the Tweet Ingester. */
object Main extends IOApp {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val kafkaSerializer: Serializer[IO, Tweet] = Serializer
    .string[IO](StandardCharsets.UTF_8)
    .contramap(tweet => tweet.asJson.spaces4)

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      config <- loadConfig()
      _ <- startApp(config)
    } yield ExitCode.Success
  }

  def startApp(config: Config): IO[Unit] = {
//    val tweetSource: Stream[IO, Tweet] =
    new TweetStreamService[IO](
      config.twitter,
      BlazeClientBuilder[IO](ec)
    ).stream()
//
//    val kafkaProducerSettings =
    ProducerSettings(
      keySerializer = Serializer[IO, String],
      valueSerializer = kafkaSerializer
    ).withBootstrapServers(config.kafka.url)

//    tweetSource.compile.drain
    IO.unit
  }

  /** Loads the application config in an effect.
    *
    * Config resolution will search in the `/resources` directory.
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
