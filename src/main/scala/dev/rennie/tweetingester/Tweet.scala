package dev.rennie.tweetingester

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}

final case class Tweet(id: Long, text: String)

object Tweet {
  implicit val decodeTweet: Decoder[Tweet] = new Decoder[Tweet] {
    override def apply(c: HCursor): Result[Tweet] =
      for {
        id <- c.downField("id").as[Long]
        text <- c.downField("text").as[String]
      } yield Tweet(id, text)
  }
}
