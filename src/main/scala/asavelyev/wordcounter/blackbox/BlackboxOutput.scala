package asavelyev.wordcounter.blackbox

import zio.blocking.Blocking
import zio.process.{ Command, CommandError }
import zio._
import zio.system._

object BlackboxOutput {
  type BlackboxOutput = Has[Service]

  private def command = property("os.name").refineOrDie(PartialFunction.empty) map {
    case Some(os) if os.toLowerCase().contains("mac") => "bin/blackbox.macosx"
    case Some(os) if os.toLowerCase().contains("win") => "bin/blackbox.win.exe"
    case _                                            => "bin/blackbox.amd64"

  }

  trait Service {
    def blackboxOutput: zio.stream.Stream[CommandError, String]
  }

  val live: ZLayer[Blocking with System, Nothing, BlackboxOutput] =
    ZLayer.fromEffect {
      ZIO.accessM { blocking =>
        command.map { command =>
          new Service {
            override def blackboxOutput: stream.Stream[CommandError, String] =
              Command(command).linesStream.provide(blocking)
          }
        }
      }
    }

  def blackboxOutput: URIO[BlackboxOutput, stream.Stream[CommandError, String]] =
    ZIO.access(_.get.blackboxOutput)
}
