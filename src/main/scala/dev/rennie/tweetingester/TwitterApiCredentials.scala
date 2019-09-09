package dev.rennie.tweetingester

import org.http4s.client.oauth1

final case class TwitterApiCredentials(consumerKey: String,
                                       consumerSecret: String,
                                       accessToken: String,
                                       accessSecret: String) {
  lazy val oauthConsumer: oauth1.Consumer =
    oauth1.Consumer(consumerKey, consumerSecret)

  lazy val oauthToken: oauth1.Token =
    oauth1.Token(accessToken, accessSecret)
}
