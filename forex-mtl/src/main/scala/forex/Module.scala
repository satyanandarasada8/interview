package forex

import cats.effect.{Concurrent, Timer}
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.programs._
import forex.services._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}

/**
  * Module
  *
  * Pure wiring layer.
  * NO service construction here.
  */
final class Module[F[_]: Concurrent: Timer](
    config: ApplicationConfig,
    ratesService: RatesService[F]      // injected
) {

  // Business logic
  private val ratesProgram: RatesProgram[F] =
    RatesProgram[F](ratesService)

  // HTTP routes
  private val ratesHttpRoutes: HttpRoutes[F] =
    new RatesHttpRoutes[F](ratesProgram).routes

  // Middleware
  private val routesMiddleware: HttpRoutes[F] => HttpRoutes[F] =
    AutoSlash(_)

  private val appMiddleware: HttpApp[F] => HttpApp[F] =
    Timeout(config.http.timeout)

  // Final app
  val httpApp: HttpApp[F] =
    appMiddleware(routesMiddleware(ratesHttpRoutes).orNotFound)
}
