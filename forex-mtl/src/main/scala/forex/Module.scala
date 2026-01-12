package forex

import cats.effect.{ Concurrent, Timer }
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.programs._
import forex.services._
import forex.services.rates.interpreters.NewOneFrame
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{ AutoSlash, Timeout }

/**
  * Module
  *
  * This is the wiring layer of the application.
  *
  * Requirement satisfied:
  * - "Application must be modular"
  * - "Services wired via dependency injection"
  */
final class Module[F[_]: Concurrent: Timer](config: ApplicationConfig) {

  /**
    * Requirement:
    * - External rate provider
    *
    * Currently using a stubbed OneFrame service.
    */
  private val ratesService: RatesService[F] =
    new NewOneFrame[F]

  /**
    * Requirement:
    * - Business logic separated from HTTP
    */
  private val ratesProgram: RatesProgram[F] =
    RatesProgram[F](ratesService)

  /**
    * Requirement:
    * - HTTP API exposing GET /rates
    */
  private val ratesHttpRoutes: HttpRoutes[F] =
    new RatesHttpRoutes[F](ratesProgram).routes

  // ---- Middleware ----

  private val routesMiddleware: HttpRoutes[F] => HttpRoutes[F] =
    AutoSlash(_)

  private val appMiddleware: HttpApp[F] => HttpApp[F] =
    Timeout(config.http.timeout)

  /**
    * Final HTTP application
    */
  val httpApp: HttpApp[F] =
    appMiddleware(routesMiddleware(ratesHttpRoutes).orNotFound)
}
