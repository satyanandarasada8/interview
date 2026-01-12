package forex.services.rates

/**
 * Error ADT for the Rates service.
 *
 * This defines all failures that can occur when
 * looking up FX rates.
 */
object errors {

  /**
   * Base trait for all rate lookup errors.
   */
  sealed trait Error

  object Error {

    /**
     * Raised when the One-Frame service fails:
     * - HTTP error
     * - Empty response
     * - Invalid payload
     */
    final case class OneFrameLookupFailed(msg: String) extends Error
  }
}
