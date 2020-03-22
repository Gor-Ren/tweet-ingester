package dev.rennie.tweetingester

import dev.rennie.tweetingester.Tweet.{TweetId, TweetMessage}
import eu.timepit.refined.refineV
import eu.timepit.refined.string.ValidLong
import org.http4s.Status
import org.scalacheck.Gen
import org.scalatest.{FunSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class BaseTestSpec extends FunSpec with Matchers with ScalaCheckPropertyChecks {
  val tweetMaxLength = 140

  val idGen: Gen[TweetId] = for {
    long <- Gen.choose(1000L, Long.MaxValue)
    id = refineV[ValidLong](long.toString).getOrElse(throw new RuntimeException)
  } yield TweetId(id)

  val tweetGen: Gen[Tweet] = for {
    id <- idGen
    text <- Gen.asciiPrintableStr
      .suchThat(_.length <= tweetMaxLength)
      .map(TweetMessage(_))
  } yield Tweet(id, text)

  val statusGen: Gen[Status] = Gen.oneOf(
    List(
      Status.Continue,
      Status.SwitchingProtocols,
      Status.Processing,
      Status.EarlyHints,
      Status.Ok,
      Status.Created,
      Status.Accepted,
      Status.NonAuthoritativeInformation,
      Status.NoContent,
      Status.ResetContent,
      Status.PartialContent,
      Status.MultiStatus,
      Status.AlreadyReported,
      Status.IMUsed,
      Status.MultipleChoices,
      Status.MovedPermanently,
      Status.Found,
      Status.SeeOther,
      Status.NotModified,
      Status.UseProxy,
      Status.TemporaryRedirect,
      Status.PermanentRedirect,
      Status.BadRequest,
      Status.Unauthorized,
      Status.PaymentRequired,
      Status.Forbidden,
      Status.NotFound,
      Status.MethodNotAllowed,
      Status.NotAcceptable,
      Status.ProxyAuthenticationRequired,
      Status.RequestTimeout,
      Status.Conflict,
      Status.Gone,
      Status.LengthRequired,
      Status.PreconditionFailed,
      Status.PayloadTooLarge,
      Status.UriTooLong,
      Status.UnsupportedMediaType,
      Status.RangeNotSatisfiable,
      Status.ExpectationFailed,
      Status.MisdirectedRequest,
      Status.UnprocessableEntity,
      Status.Locked,
      Status.FailedDependency,
      Status.TooEarly,
      Status.UpgradeRequired,
      Status.PreconditionRequired,
      Status.TooManyRequests,
      Status.RequestHeaderFieldsTooLarge,
      Status.UnavailableForLegalReasons,
      Status.InternalServerError,
      Status.NotImplemented,
      Status.BadGateway,
      Status.ServiceUnavailable,
      Status.GatewayTimeout,
      Status.HttpVersionNotSupported,
      Status.VariantAlsoNegotiates,
      Status.InsufficientStorage,
      Status.LoopDetected,
      Status.NotExtended,
      Status.NetworkAuthenticationRequired
    )
  )
}
