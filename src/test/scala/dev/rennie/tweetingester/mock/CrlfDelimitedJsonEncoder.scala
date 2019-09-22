package dev.rennie.tweetingester.mock

import java.nio.charset.StandardCharsets

import cats.Applicative
import dev.rennie.tweetingester.Tweet
import fs2.Stream
import io.circe.{Encoder, Json, Printer}
import io.circe.syntax._
import org.http4s
import org.http4s.circe.CirceInstances
import org.http4s.headers.{`Content-Type`, `Transfer-Encoding`}
import org.http4s.{
  Entity,
  EntityBody,
  EntityEncoder,
  Headers,
  MediaType,
  TransferCoding
}

/** Encodes sequences of objects as CRLF-delimited JSON value streams.
  *
  * Given a streamed sequence of objects `O`, encodes each `O` as a JSON object
  * separated by `\r\n` (CRLF). The JSON objects may contain newlines, `\n`, but
  * not the CRLF sequence.
  */
class CrlfDelimitedJsonEncoder[F[_]: Applicative, O](
    implicit jsonEncoder: Encoder[O]
) extends EntityEncoder[F, Stream[F, Seq[O]]] {

  private val delimiter: String = "\r\n"
  private val delimiterBody: EntityBody[F] =
    Stream.emits(delimiter.getBytes(StandardCharsets.UTF_8))

  val jsonEntityEncoder: EntityEncoder[F, Json] = CirceInstances
    .withPrinter(Printer.spaces4) // output with indentation and line feeds
    .build
    .jsonEncoder[F]

  override def toEntity(stream: Stream[F, Seq[O]]): Entity[F] = Entity(
    for {
      els <- stream
      el <- Stream.emits(els)
      delimited <- jsonEntityEncoder.toEntity(el.asJson).body ++ delimiterBody
    } yield delimited
  )

  override def headers: Headers = Headers.of(
    `Content-Type`(MediaType.application.json, http4s.Charset.`UTF-8`),
    `Transfer-Encoding`(TransferCoding.chunked)
  )
}

object CrlfDelimitedJsonEncoderInstances {
  implicit def tweetDelimitedJsonEncoder[F[_]: Applicative](
      implicit jsonEncoder: Encoder[Tweet]
  ): EntityEncoder[F, Stream[F, Seq[Tweet]]] =
    new CrlfDelimitedJsonEncoder[F, Tweet]()
}
