package forex.services.rates.interpreters

import cats.effect.Sync
import forex.domain.{Rate, Price, Timestamp}
import forex.services.rates.Algebra
import forex.services.rates.errors.Error

/**
  * Stub implementation of the OneFrame rate provider.
  *
  * ============================================================
  * WHY THIS FILE EXISTS
  * ============================================================
  *
  * The assignment requires us to fetch FX rates from "OneFrame".
  * Before adding HTTP (which adds complexity),
  * we create a *stub* implementation.
  *
  * This allows us to:
  *  - satisfy the required interfaces (Algebra)
  *  - keep the application compiling
  *  - wire services together safely
  *
  * This is an intentional step, not a shortcut.
  */

final class NewOneFrame[F[_]: Sync] extends Algebra[F] {

  /**
    * ============================================================
    * REQUIREMENT SATISFIED
    * ============================================================
    *
    * - "The system must provide exchange rates for a currency pair"
    *
    * At this stage:
    *  - We DO NOT call real HTTP
    *  - We return a hard-coded (dummy) rate
    *
    * This will later be replaced by a real HTTP implementation.
    */

  override def get(pair: Rate.Pair): F[Either[Error, Rate]] =

    /**
      * Sync[F].pure(...) means:
      *
      * - Wrap a value into the effect F
      * - Do NOT perform side effects
      *
      * Example:
      *   If F = IO, this becomes IO.pure(...)
      */
    Sync[F].pure {

      /**
        * We always return Right(...) here,
        * meaning "success".
        *
        * Error handling will be added later
        * when HTTP is introduced.
        */
      Right(
        Rate(
          pair = pair,

          // Dummy price for now
          // Requirement note:
          // This satisfies the "return a rate" contract
          price = Price(BigDecimal(1.0)),

          // Current timestamp
          // This shows we respect the domain model
          timestamp = Timestamp.now
        )
      )
    }
}
