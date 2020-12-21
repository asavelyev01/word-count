package asavelyev.wordcounter

import asavelyev.wordcounter.blackbox.BlackboxOutput
import asavelyev.wordcounter.blackbox.BlackboxOutput.BlackboxOutput
import zio._
import io.circe.generic.auto._
import io.circe.parser._
import zio.console._
import cats.syntax.all._
import zio.process.CommandError

object WordCounterService {
  type WordCounterService = Has[Service]
  trait Service {
    def wordCount: IO[CommandError, WordCount]
  }

  val impl: ZLayer[Console with BlackboxOutput, Nothing, WordCounterService] =
    ZLayer.fromManaged {
      for {
        state <- ZManaged.fromEffect(Ref.make(WordCount.empty))
        _     <- daemon(state).forkManaged
      } yield new Service {
        override def wordCount = state.get
      }
    }

  private def parseLine(line: String) =
    parse(line).flatMap(_.as[BlackboxLine])

  private def daemon(state: Ref[WordCount]): ZIO[Console with BlackboxOutput, CommandError, Unit] =
    BlackboxOutput.blackboxOutput.flatMap {
      _.map(parseLine).foreach {
        case Right(BlackboxLine(eventType, word)) => state.update(_ |+| WordCount.one(eventType, word))
        case Left(value)                          => putStr(s"Failed to parse line because $value")
      }
    }

  def wordCount: ZIO[WordCounterService, CommandError, WordCount] =
    ZIO.accessM(_.get.wordCount)
}
