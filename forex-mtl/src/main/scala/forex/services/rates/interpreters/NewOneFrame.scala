package forex.services.rates.interpreters

import cats.effect.Concurrent
import cats.syntax.functor._            // enables .map on F[_]

import forex.domain._
import forex.services.RatesService
import forex.services.rates.errors.Error
import forex.services.rates.oneframe.OneFrameRateDto

import org.http4s._
import org.http4s.client.Client
import org.http4s.circe.CirceEntityDecoder._ // JSON decoding via Circe
import org.typelevel.ci.CIString

import java.time.OffsetDateTime

/**
 * Live One-Frame interpreter.
 *
 * Talks to the One-Frame HTTP API and converts the response
 * into our domain Rate model.
 *
 * CE2 + http4s 0.22 compatible.
 */
final class NewOneFrame[F[_]: Concurrent](
  client: Client[F],   // injected http4s client
  baseUri: Uri,        // e.g. http://localhost:8080
  token: String        // One-Frame auth token
) extends RatesService[F] {

  /**
   * Fetch a rate for a given currency pair.
   *
   * Example:
   *   pair = USD / JPY
   *   GET /rates?pair=USDJPY
   */
  override def get(pair: Rate.Pair): F[Either[Error, Rate]] = {

    // Build URI: /rates?pair=USDJPY
    val uri =
  baseUri
    .withPath(Uri.Path.unsafeFromString("/rates"))
    .withQueryParam("pair", s"${pair.from}${pair.to}")

    // Build request with auth header
    val request =
      Request[F](Method.GET, uri)
        .putHeaders(
          Header.Raw(CIString("token"), token)
        )

    // Execute request safely using Resource
    client.run(request).use { response =>
      response.status match {

        // -------- SUCCESS --------
        case Status.Ok =>
          response
            .as[List[OneFrameRateDto]]
            .map { rates =>
              rates.headOption match {

                case Some(r) =>
                  Right(
                    Rate(
                      pair = pair,
                      price = Price(r.price),
                      timestamp =
                        Timestamp(
                          OffsetDateTime.parse(r.time_stamp)
                        )
                    )
                  )

                case None =>
                  Left(
                    Error.OneFrameLookupFailed(
                      "One-Frame returned empty rate list"
                    )
                  )
              }
            }

        // -------- FAILURE --------
        case _ =>
          Concurrent[F].pure(
            Left(
              Error.OneFrameLookupFailed(
                s"One-Frame request failed with status ${response.status}"
              )
            )
          )
      }
    }
  }
}
