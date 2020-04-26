package dev.rennie.tweetingester

import java.nio.charset.StandardCharsets

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.flatMap._
import cats.syntax.show._
import dev.rennie.tweetingester.client.LiveTwitterClient
import dev.rennie.tweetingester.config.AppConfig
import dev.rennie.tweetingester.domain.Tweet
import fs2.kafka.{ProducerSettings, Serializer}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.syntax._
import javax.naming.ConfigurationException
import pureconfig.ConfigSource
import pureconfig.generic.auto._

/** Application entry point for the Tweet Ingester. */
object Main extends IOApp {

  val kafkaSerializer: Serializer[IO, Tweet] = Serializer
    .string[IO](StandardCharsets.UTF_8)
    .contramap(tweet => tweet.asJson.spaces4)

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      implicit0(logger: Logger[IO]) <- Slf4jLogger.create[IO]
      config <- loadConfig(logger)
      _ <- logger.info(s"Config loaded:\n${config.show}")
      _ <- startApp(config)
    } yield ExitCode.Success
  }

  def startApp(config: AppConfig)(implicit logger: Logger[IO]): IO[Unit] = {
//    val tweetSource: Stream[IO, Tweet] =
    LiveTwitterClient.make[IO](config.twitter)
//
//    val kafkaProducerSettings =
    ProducerSettings(
      keySerializer = Serializer[IO, String],
      valueSerializer = kafkaSerializer
    ).withBootstrapServers(config.kafka.url.value)

//    tweetSource.compile.drain
    logger.info("Starting app...") >> IO.unit
  }

  def loadConfig(implicit logger: Logger[IO]): IO[AppConfig] = {
    // note: config resolution will search in the `/resources` directory
    logger.info("Loading config...") >> IO.fromEither[AppConfig](
      ConfigSource.default
        .load[AppConfig]
        .left
        .map(f => new ConfigurationException(f.toString))
    )
  }
}
