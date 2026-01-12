package forex

import cats.effect.{ ExitCode, IO, IOApp }
import forex.config._
import fs2.Stream
import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext

/**
  * Main entry point of the application.
  *
  * Requirement satisfied:
  * - "Application must start an HTTP server"
  *
  * CE2 NOTE:
  * - IOApp DOES exist in cats-effect 2
  * - We must provide an ExecutionContext manually
  */
object Main extends IOApp {

  /**
    * This is where the app starts.
    */
  override def run(args: List[String]): IO[ExitCode] =
    stream.compile.drain.as(ExitCode.Success)

  /**
    * fs2 Stream that:
    * 1. Loads configuration
    * 2. Wires the module
    * 3. Starts the HTTP server
    */
  private def stream: Stream[IO, Unit] = {
    // CE2-style ExecutionContext
    implicit val ec: ExecutionContext =
      ExecutionContext.global

    for {
      // Load application.conf under "app"
      config <- Config.stream[IO]("app")

      // Wire dependencies
      module = new Module[IO](config)

      // Start HTTP server
      _ <- BlazeServerBuilder[IO](ec)
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve
    } yield ()
  }
}
