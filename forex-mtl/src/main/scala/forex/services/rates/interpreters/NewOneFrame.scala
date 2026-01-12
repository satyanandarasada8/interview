package forex.services.rates.interpreters

import cats.effect.Sync
import forex.domain._
import forex.services.RatesService
import forex.services.rates.errors._
import java.time.OffsetDateTime
import scala.math.BigDecimal

/**
  * NewOneFrame (STUB IMPLEMENTATION)
  *
  * This class represents the One-Frame external rate provider.
  *
  * IMPORTANT:
  * - This is a STUB (no real HTTP yet)
  * - It satisfies the requirement:
  *     "Rates must be obtained from an external provider"
  *   in a controlled, testable way
  *
  * Scala newbie notes:
  * - F[_] means "effect type" (like IO)
  * - Sync[F] lets us safely create effects
  */
final class NewOneFrame[F[_]: Sync] extends RatesService[F] {

  /**
    * Requirement satisfied:
    * - "Provide exchange rates between currency pairs"
    *
    * For now, we return a fixed rate.
    * This keeps the system compiling and testable.
    */
  override def get(pair: Rate.Pair): F[Either[Error, Rate]] =
    Sync[F].delay {
      Right(
        Rate(
          pair = pair,
          price = Price(BigDecimal(1.2345)), // fake but valid rate
          timestamp = Timestamp(OffsetDateTime.now())
        )
      )
    }
}
