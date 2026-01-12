package forex.services.rates

import forex.domain.Rate
import errors._

/**
 * Algebra for FX rate lookup.
 *
 * Defines WHAT the service does, not HOW.
 */
trait Algebra[F[_]] {

  /**
   * Fetch the FX rate for a currency pair.
   *
   * @param pair currency pair (e.g. USD/JPY)
   * @return either a domain Error or a valid Rate
   */
  def get(pair: Rate.Pair): F[Either[Error, Rate]]
}
