package dev.rennie.tweetingester.config
import cats.Show
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import eu.timepit.refined.pureconfig._
import eu.timepit.refined.types.string.NonEmptyString
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class KafkaConfig(url: String Refined Url, topicName: NonEmptyString)

object KafkaConfig {
  implicit val showKafkaConfig: Show[KafkaConfig] = Show.fromToString

  implicit val readKafkaConfig: ConfigReader[KafkaConfig] = deriveReader[KafkaConfig]
}
