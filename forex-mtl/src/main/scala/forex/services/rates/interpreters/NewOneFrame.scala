package forex.services.rates.interpreters

import cats.effect.Sync
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.Algebra
import forex.services.rates.errors.Error

/**
  * OneFrame stub implementation (happy path only).
  *
  * - No HTTP
  * - No auth token
  * - No failure cases
  *
  * Purpose:
  *   Keep the architecture intact while allowing the system
  *   to compile and run end-to-end.
  */
final class NewOneFrame[F[_]: Sync] extends Algebra[F] {

  override def get(
      pair: Rate.Pair
  ): F[Either[Error, Rate]] =
    Sync[F].pure {
      Right(
        Rate(
          pair = pair,
          price = Price(BigDecimal(1.2345)),
          timestamp = Timestamp.now
        )
      )
    }
}
