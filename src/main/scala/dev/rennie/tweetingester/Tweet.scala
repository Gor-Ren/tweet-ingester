package dev.rennie.tweetingester

import io.circe.{Decoder, Encoder, HCursor}
import io.circe.generic.semiauto.deriveEncoder

final case class Tweet(id: Long, text: String)

object Tweet {
  implicit val tweetDecoder: Decoder[Tweet] = (c: HCursor) =>
    for {
      id <- c.downField("id").as[Long]
      text <- c.downField("text").as[String]
    } yield Tweet(id, text)

  implicit val tweetEncoder: Encoder[Tweet] = deriveEncoder[Tweet]
}
