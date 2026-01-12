package forex.services.rates.oneframe

import io.circe.Decoder
import io.circe.generic.semiauto._

/**
 * DTO representing One-Frame API JSON response.
 *
 * Example JSON:
 * {
 *   "from": "USD",
 *   "to": "JPY",
 *   "price": 0.71,
 *   "time_stamp": "2019-01-01T00:00:00.000"
 * }
 */
final case class OneFrameRateDto(
  from: String,
  to: String,
  price: BigDecimal,
  time_stamp: String
)

object OneFrameRateDto {

  /**
   * Circe decoder for OneFrameRateDto.
   * Required for JSON → Scala conversion.
   */
  implicit val decoder: Decoder[OneFrameRateDto] =
    deriveDecoder
}
