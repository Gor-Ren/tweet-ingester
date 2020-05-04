package dev.rennie.tweetingester.domain

import cats.{Eq, Show}
import cats.syntax.contravariant._
import cats.instances.string._
import cats.instances.eq._
import dev.rennie.tweetingester.domain
import dev.rennie.tweetingester.domain.Tweet.{TweetId, TweetMessage}
import dev.rennie.tweetingester.util.NewtypeSupport.NewtypeCats
import dev.rennie.tweetingester.util.NewtypeSupport.NewtypeCats._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.ValidLong
import eu.timepit.refined.cats._
import eu.timepit.refined.cats.derivation._
import io.circe.generic.semiauto.deriveEncoder
import io.circe.refined._
import io.circe.{Decoder, Encoder, HCursor}
import io.estatico.newtype.macros._

final case class Tweet(id: TweetId, text: TweetMessage)

object Tweet {
  @newtype case class TweetId(value: String Refined ValidLong)
  @newtype case class TweetMessage(value: String)

//  implicit val tweetIdShow: Show[TweetId] = Show[String Refined ValidLong].contramap(_.value)
//  implicit val TweetMessageShow: Show[TweetMessage] = Show[String].contramap(_.value)
//  implicit val tweetIdEq: Eq[TweetId] = Eq.by[TweetId, String Refined ValidLong](_.value)
//  implicit val TweetMessageEq: Eq[TweetMessage] = Eq.by[TweetMessage, String](_.value)

  implicit val tweetEq: Eq[Tweet] = {
    implicit val test: Eq[TweetId] =
      NewtypeCats.coercibleRefinedEq[Refined, String, ValidLong, TweetId]
    import cats.derived.auto.eq._
    cats.derived.semi.eq
  }

  implicit val tweetShow: Show[Tweet] = {
    implicit val test2: Show[TweetId] =
      NewtypeCats.coercibleRefinedShow[Refined, String, ValidLong, TweetId]
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
