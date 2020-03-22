package dev.rennie.tweetingester

import java.nio.charset.StandardCharsets

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.flatMap._
import cats.syntax.show._
import fs2.kafka.{ProducerSettings, Serializer}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
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
      logger <- Slf4jLogger.create[IO]
      config <- loadConfig(logger)
      _ <- logger.info(s"Config loaded:\n${config.show}")
      _ <- startApp(config)(logger)
    } yield ExitCode.Success
  }

  def startApp(config: Config)(implicit logger: Logger[IO]): IO[Unit] = {
//    val tweetSource: Stream[IO, Tweet] =
    new TwitterClient[IO](
      config.twitter,
      BlazeClientBuilder[IO](ec)
    ).stream
//
//    val kafkaProducerSettings =
    ProducerSettings(
      keySerializer = Serializer[IO, String],
      valueSerializer = kafkaSerializer
    ).withBootstrapServers(config.kafka.url)

//    tweetSource.compile.drain
    logger.info("Starting app...") >> IO.unit
  }

  /** Loads the application config in an effect.
    *
    * Config resolution will search in the `/resources` directory.
    */
  def loadConfig(implicit log: Logger[IO]): IO[Config] =
    log.info("Loading config...") >> IO.fromEither[Config](
      ConfigSource.default
        .load[Config]
        .left
        .map(f => new ConfigurationException(f.toString))
    )
}
