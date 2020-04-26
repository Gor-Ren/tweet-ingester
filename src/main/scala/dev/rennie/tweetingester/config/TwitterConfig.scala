package dev.rennie.tweetingester.config

import cats.Show
import cats.syntax.show._
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.pureconfig._
import org.http4s.Uri
import org.http4s.client.oauth1
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import pureconfig.generic.semiauto.deriveReader

final case class TwitterConfig(
    sampleEndpoint: Uri,
    credentials: TwitterApiCredentials
)

object TwitterConfig {
  implicit val showTwitterConfig: Show[TwitterConfig] = Show { config =>
    s"TwitterConfig(endpoint=${config.sampleEndpoint},credentials=${config.credentials.show}"
  }

  implicit val readUri: ConfigReader[Uri] = ConfigReader.fromString(s =>
    Uri
      .fromString(s)
      .left
      .map(err => CannotConvert(s, "http4s.Uri", err.message))
  )

  implicit val readTwitterConfig: ConfigReader[TwitterConfig] = deriveReader[TwitterConfig]
}

final case class TwitterApiCredentials(
    consumerKey: NonEmptyString,
    consumerSecret: NonEmptyString,
    accessToken: NonEmptyString,
    accessSecret: NonEmptyString
) {
  val oauthConsumer = oauth1.Consumer(consumerKey.value, consumerSecret.value)

  val oauthToken = oauth1.Token(accessToken.value, accessSecret.value)
}

object TwitterApiCredentials {
  implicit val showTwitterCreds: Show[TwitterApiCredentials] = Show { _ =>
    "TwitterApiCredentials(***REDACTED***)"
  }

  implicit val readTwitterApiCredentials: ConfigReader[TwitterApiCredentials] =
    deriveReader[TwitterApiCredentials]
}
