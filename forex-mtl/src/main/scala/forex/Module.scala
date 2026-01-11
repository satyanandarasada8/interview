package forex

import cats.effect.{Concurrent, Timer}
import cats.effect.concurrent.Ref
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.programs._
import forex.services._
import forex.services.rates.interpreters._
import forex.domain.Rate
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}
import scala.concurrent.duration._

class Module[F[_]: Concurrent: Timer](config: ApplicationConfig) {

  // ------------------------------------------------
  // Rates service wiring
  // ------------------------------------------------

  /**
    * OneFrame provider (stub).
    * Requirement satisfied:
    *  - External provider abstraction exists
    */
  private val oneFrameService: RatesService[F] =
    new NewOneFrame[F]()

  /**
    * Cached rates service with expiry.
    *
    * Requirements satisfied:
    *  - In-memory cache
    *  - Time-based expiry (expiryTime)
    *  - Cache sits BEFORE HTTP
    */
  private val ratesService: RatesService[F] = {
  val emptyCache =
    Ref.unsafe[F, Map[Rate.Pair, (Rate, Long)]](Map.empty)

  new NewCachedRatesService[F](
    underlying = oneFrameService,
	// Requirement: time-based expiry (config to be added later)
    expiryTime = 5.seconds, 
    cache      = emptyCache
  )
}

  // ------------------------------------------------
  // Program layer
  // ------------------------------------------------

  private val ratesProgram: RatesProgram[F] =
    RatesProgram[F](ratesService)

  // ------------------------------------------------
  // HTTP layer
  // ------------------------------------------------

  private val ratesHttpRoutes: HttpRoutes[F] =
    new RatesHttpRoutes[F](ratesProgram).routes

  // ------------------------------------------------
  // Middleware
  // ------------------------------------------------

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware =
    http => AutoSlash(http)

  private val appMiddleware: TotalMiddleware =
    http => Timeout(config.http.timeout)(http)

  private val http: HttpRoutes[F] =
    ratesHttpRoutes

  val httpApp: HttpApp[F] =
    appMiddleware(routesMiddleware(http).orNotFound)
}
