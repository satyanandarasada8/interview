package forex

import cats.effect._
import forex.config._
import forex.services.rates.interpreters._
import org.http4s.Uri
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder


import scala.concurrent.ExecutionContext

/**
  * Main entry point (Cats Effect 2, fs2-based config)
  */
object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    implicit val ec: ExecutionContext =
      ExecutionContext.global

    Config
      .stream[IO]("app")               // <-- THIS is what your repo provides
      .evalMap { config =>

        BlazeClientBuilder[IO](ec).resource.use { client =>

          val oneFrame =
            new NewOneFrame[IO](
              client  = client,
              baseUri = Uri.unsafeFromString("http://localhost:8080"),
              token   = "10dc303535874aeccc86a8251e6992f5"
            )

          NewCachedRatesService
            .make[IO](
              underlying = oneFrame,
              expiryTime = config.http.timeout
            )
            .flatMap { ratesService =>

              val module =
                new Module[IO](
                  config       = config,
                  ratesService = ratesService
                )

              BlazeServerBuilder[IO](ec)
                .bindHttp(config.http.port, config.http.host)
                .withHttpApp(module.httpApp)
                .serve
                .compile
                .drain
            }
        }
      }
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
