package dev.rennie.tweetingester

import org.http4s.Uri
import org.http4s.client.oauth1

case class Config(twitter: TwitterConfig)

// TODO: awaiting pureconfig-http4s update for http4s.Uri support for endpoint
case class TwitterConfig(endpoint: String, credentials: TwitterApiCredentials) {
  def endpointUri: Uri = Uri.unsafeFromString(endpoint)
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
