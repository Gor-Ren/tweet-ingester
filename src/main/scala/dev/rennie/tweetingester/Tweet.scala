package dev.rennie.tweetingester

import dev.rennie.tweetingester.Tweet.{TweetId, TweetMessage}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.ValidLong
import io.circe.generic.semiauto.deriveEncoder
import io.circe.refined._
import io.circe.{Decoder, Encoder, HCursor}
import io.estatico.newtype.macros._

final case class Tweet(id: TweetId, text: TweetMessage)

object Tweet {
  @newtype case class TweetId(value: String Refined ValidLong)
  @newtype case class TweetMessage(value: String)

  object TweetId {
    implicit val idDecoder: Decoder[TweetId] =
      Decoder[String Refined ValidLong].map(TweetId(_))

    implicit val idEncoder: Encoder[TweetId] =
      Encoder[String Refined ValidLong].contramap(_.value)
  }

  object TweetMessage {
    implicit val tweetMessageDecoder: Decoder[TweetMessage] =
      Decoder[String].map(TweetMessage(_))

    implicit val tweetMessageidEncoder: Encoder[TweetMessage] =
      Encoder[String].contramap(_.value)
  }

  implicit val tweetDecoder: Decoder[Tweet] = (c: HCursor) =>
    for {
      id <- c.downField("id").as[TweetId]
      text <- c.downField("text").as[TweetMessage]
    } yield Tweet(id, text)

  implicit val tweetEncoder: Encoder[Tweet] = deriveEncoder[Tweet]
}
