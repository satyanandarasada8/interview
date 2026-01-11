package forex.services.rates.interpreters

import cats.effect.{Clock, Sync}
import cats.effect.concurrent.Ref
import cats.syntax.flatMap._
import cats.syntax.functor._
import forex.domain.Rate
import forex.services.RatesService
import forex.services.rates.errors.Error

import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

/**
  * Cached implementation of RatesService.
  *
  * ================= REQUIREMENTS SATISFIED =================
  *
  * ✔ Requirement: Rates should be cached in-memory
  * ✔ Requirement: Cached rates should expire after a fixed duration
  * ✔ Requirement: Cache must be transparent to callers
  * ✔ Requirement: No HTTP logic in cache layer
  *
  * ==========================================================
  */
final class NewCachedRatesService[F[_]: Sync: Clock](
    underlying: RatesService[F],                      // underlying rate provider
    expiryTime: FiniteDuration,                        // cache expiry duration
    cache: Ref[F, Map[Rate.Pair, (Rate, Long)]]        // in-memory cache
) extends RatesService[F] {

  /**
    * Get a rate for a currency pair.
    *
    * This method first checks the cache.
    * If the cached value is missing or expired,
    * it delegates to the underlying service.
    */
  override def get(pair: Rate.Pair): F[Either[Error, Rate]] =
    for {
      // Requirement: expiry based on current wall-clock time
      nowMillis <- Clock[F].realTime(TimeUnit.MILLISECONDS)

      // Read current cache state
      state <- cache.get

      result <- state.get(pair) match {

        // -------------------------------
        // Cache HIT and still valid
        // -------------------------------
        case Some((rate, cachedAt)) if isFresh(cachedAt, nowMillis) =>
          Sync[F].pure(Right(rate))

        // -------------------------------
        // Cache MISS or expired
        // -------------------------------
        case _ =>
          fetchAndUpdate(pair, nowMillis)
      }
    } yield result

  /**
    * Checks whether a cached entry is still valid.
    *
    * Requirement satisfied:
    * ✔ Cached rates must expire after expiryTime
    */
  private def isFresh(cachedAt: Long, now: Long): Boolean =
    (now - cachedAt) <= expiryTime.toMillis

  /**
    * Fetches from underlying service and updates cache on success.
    *
    * Requirement satisfied:
    * ✔ Cache updated only on successful fetch
    * ✔ Errors do NOT pollute cache
    */
  private def fetchAndUpdate(
      pair: Rate.Pair,
      nowMillis: Long
  ): F[Either[Error, Rate]] =
    underlying.get(pair).flatTap {
      case Right(rate) =>
        cache.update(_.updated(pair, (rate, nowMillis)))

      case Left(_) =>
        Sync[F].unit
    }
}

/**
  * Companion object for safe construction.
  *
  * Requirement satisfied:
  * ✔ Cache starts empty
  */
object NewCachedRatesService {

  def make[F[_]: Sync: Clock](
      underlying: RatesService[F],
      expiryTime: FiniteDuration
  ): F[RatesService[F]] =
    Ref
      .of[F, Map[Rate.Pair, (Rate, Long)]](Map.empty)
      .map(ref => new NewCachedRatesService[F](underlying, expiryTime, ref))
}
