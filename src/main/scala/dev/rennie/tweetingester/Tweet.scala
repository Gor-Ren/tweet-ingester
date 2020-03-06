package dev.rennie.tweetingester

import dev.rennie.tweetingester.Tweet.Id
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.ValidLong
import io.circe.generic.semiauto.deriveEncoder
import io.circe.refined._
import io.circe.{Decoder, Encoder, HCursor}
import io.estatico.newtype.macros._

final case class Tweet(id: Id, text: String)

object Tweet {
  @newtype case class Id(value: String Refined ValidLong)

  object Id {
    implicit val idDecoder: Decoder[Id] =
      Decoder[String Refined ValidLong].map(Id(_))

    implicit val idEncoder: Encoder[Id] =
      Encoder[String Refined ValidLong].contramap(_.value)
  }

  implicit val tweetDecoder: Decoder[Tweet] = (c: HCursor) =>
    for {
      id <- c.downField("id").as[Id]
      text <- c.downField("text").as[String]
    } yield Tweet(id, text)

  implicit val tweetEncoder: Encoder[Tweet] = deriveEncoder[Tweet]
}
