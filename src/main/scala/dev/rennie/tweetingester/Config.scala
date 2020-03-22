package dev.rennie.tweetingester

import cats.Show
import cats.syntax.show._
import org.http4s.Uri
import org.http4s.client.oauth1

case class Config(twitter: TwitterConfig, kafka: KafkaConfig)

object Config {
  implicit val showConfig: Show[Config] = Show { config =>
    s"Config(\n\ttwitter=${config.twitter.show},\n\tkafka=${config.kafka.show}\n)"
  }
}

case class TwitterConfig(endpoint: String, credentials: TwitterApiCredentials) {
  def endpointUri: Uri = Uri.unsafeFromString(endpoint)
}

object TwitterConfig {

  implicit val showTwitterConfig: Show[TwitterConfig] = Show { config =>
    s"TwitterConfig(endpoint=${config.endpoint},credentials=${config.credentials.show}"
  }
}

case class TwitterApiCredentials(consumerKey: String,
                                 consumerSecret: String,
                                 accessToken: String,
                                 accessSecret: String) {
  lazy val oauthConsumer: oauth1.Consumer =
    oauth1.Consumer(consumerKey, consumerSecret)

  lazy val oauthToken: oauth1.Token =
    oauth1.Token(accessToken, accessSecret)
}

object TwitterApiCredentials {
  implicit val showTwitterCreds: Show[TwitterApiCredentials] = Show { _ =>
    "TwitterApiCredentials(***REDACTED***)"
  }
}

case class KafkaConfig(url: String, topicName: String)

object KafkaConfig {
  implicit val showKafkaConfig: Show[KafkaConfig] = Show.fromToString
}
