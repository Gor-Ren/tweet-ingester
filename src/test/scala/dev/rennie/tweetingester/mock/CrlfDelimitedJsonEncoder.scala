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
  * separated by `\r\n` (CRLF). The JSON objects may contain newlines `\n` but
  * not the CRLF sequence.
  *
  * Optionally, `\n` elements may appear between tweets which is used to keep
  * a connection alive when no tweets are being emitted.
  */
class CrlfDelimitedJsonEncoder[F[_]: Applicative, O](keepAlive: Boolean)(
    implicit jsonEncoder: Encoder[O]
) extends EntityEncoder[F, Stream[F, Seq[O]]] {

  /** Delimits tweets present within the response body. */
  private val delimiterBody: EntityBody[F] =
    Stream.emits("\r\n".getBytes(StandardCharsets.UTF_8))

  /** May appear between tweets in order to keep the connection alive. */
  private val keepAliveBody: EntityBody[F] =
    Stream.emits("\n".getBytes(StandardCharsets.UTF_8))

  val jsonEntityEncoder: EntityEncoder[F, Json] = CirceInstances
    .withPrinter(Printer.spaces4) // output with indentation and line feeds
    .build
    .jsonEncoder[F]

  override def toEntity(stream: Stream[F, Seq[O]]): Entity[F] = Entity(
    for {
      els <- stream
      el <- Stream.emits(els)
      delimiters = if (keepAlive)
        delimiterBody ++ keepAliveBody
      else
        delimiterBody

      delimited <- jsonEntityEncoder.toEntity(el.asJson).body ++ delimiters
    } yield delimited
  )

  override def headers: Headers = Headers.of(
    `Content-Type`(MediaType.application.json, http4s.Charset.`UTF-8`),
    `Transfer-Encoding`(TransferCoding.chunked)
  )
}

object CrlfDelimitedJsonEncoderInstances {

  /** Encodes [[Tweet]]s, delimiting each element with `\r\n`. */
  implicit def tweetDelimitedJsonEncoder[F[_]: Applicative](
      implicit jsonEncoder: Encoder[Tweet]
  ): EntityEncoder[F, Stream[F, Seq[Tweet]]] =
    new CrlfDelimitedJsonEncoder[F, Tweet](false)

  /** Encodes [[Tweet]]s, delimiting each element with `\r\n` and `\n`.
    *
    * The `\n` simulates an output used by Twitter to keep a connection alive.
    */
  implicit def tweetDelimitedKeepAliveJsonEncoder[F[_]: Applicative](
      implicit jsonEncoder: Encoder[Tweet]
  ): EntityEncoder[F, Stream[F, Seq[Tweet]]] =
    new CrlfDelimitedJsonEncoder[F, Tweet](true)
}
