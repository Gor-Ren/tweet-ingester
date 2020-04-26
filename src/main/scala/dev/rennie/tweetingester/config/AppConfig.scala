package dev.rennie.tweetingester.config

import cats.Show
import cats.syntax.show._
import org.http4s.Uri
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import pureconfig.generic.semiauto._

case class AppConfig(twitter: TwitterConfig, kafka: KafkaConfig)

object AppConfig {
  implicit val showConfig: Show[AppConfig] = Show { config =>
    s"Config(\n\ttwitter=${config.twitter.show},\n\tkafka=${config.kafka.show}\n)"
  }

  implicit val readAppConfig: ConfigReader[AppConfig] = deriveReader[AppConfig]
}
