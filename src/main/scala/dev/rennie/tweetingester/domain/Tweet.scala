package dev.rennie.tweetingester.domain

import cats.{Eq, Show}
import dev.rennie.tweetingester.domain
import dev.rennie.tweetingester.domain.Tweet.{TweetId, TweetMessage}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.ValidLong
import eu.timepit.refined.cats._
import eu.timepit.refined.cats.derivation._
import io.circe.generic.semiauto.deriveEncoder
import io.circe.refined._
import io.circe.{Decoder, Encoder, HCursor}
import io.estatico.newtype.macros._
import dev.rennie.tweetingester.util.NewtypeSupport.NewtypeCats._

final case class Tweet(id: TweetId, text: TweetMessage)

object Tweet {
  @newtype case class TweetId(value: String Refined ValidLong)
  @newtype case class TweetMessage(value: String)

  // TODO: why must these be summoned here? NewtypeSupport.NewtypeCats._ should be sufficient...
  implicit val tweetIdEq: Eq[TweetId] = implicitly
  implicit val tweetIdShow: Show[TweetId] = implicitly

  implicit val tweetMessageEq: Eq[TweetMessage] = implicitly
  implicit val tweetMessageShow: Show[TweetMessage] = implicitly

  implicit val tweetEq: Eq[Tweet] = {
    import cats.derived.auto.eq._
    cats.derived.semi.eq
  }

  implicit val tweetShow: Show[Tweet] = {
    import cats.derived.auto.show._
    cats.derived.semi.show
  }

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
    } yield domain.Tweet(id, text)

  implicit val tweetEncoder: Encoder[Tweet] = deriveEncoder[Tweet]
}
