package asavelyev.wordcounter

import asavelyev.wordcounter.WordCounterService.WordCounterService
import asavelyev.wordcounter.blackbox.BlackboxOutput
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio.blocking.Blocking
import zio.console.{ Console, putStrLn }
import zio.{ App, ExitCode, URIO, ZIO, _ }
import cats.effect.{ ExitCode => CatsExitCode }
import zio.clock.Clock
import zio.interop.catz._

object Main extends App {

  override def run(args: List[String]): URIO[Console, ExitCode] =
    program
      .provideSomeLayer(appEnvironment)
      .tapError(err => putStrLn(s"Execution failed with: $err"))
      .exitCode

  type AppEnvironment = WordCounterService with Clock

  val appEnvironment =
    Blocking.live >+>
      zio.system.System.live >+>
      BlackboxOutput.live >+> Console.live >+>
      WordCounterService.impl >+> Clock.live

  type AppTask[A] = RIO[AppEnvironment, A]

  val httpApp = Router[AppTask](
    "/words" -> (new Api).route
  ).orNotFound

  val program =
    for {
      server <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
                  BlazeServerBuilder[AppTask]
                    .bindHttp(8080)
                    .withHttpApp(CORS(httpApp))
                    .serve
                    .compile[AppTask, AppTask, CatsExitCode]
                    .drain
                }
    } yield server
}
