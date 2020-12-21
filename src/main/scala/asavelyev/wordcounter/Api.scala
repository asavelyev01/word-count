package asavelyev.wordcounter

import asavelyev.wordcounter.WordCounterService.WordCounterService
import io.circe.{ Decoder, Encoder, Json }
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{ EntityDecoder, EntityEncoder, HttpRoutes }
import zio._
import zio.interop.catz._
import WordCounterService._
import asavelyev.wordcounter.Count.Syntax
import cats.Show

class Api[R <: WordCounterService] {

  type UserTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UserTask, A] = jsonOf[UserTask, A]

  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UserTask, A] =
    jsonEncoderOf[UserTask, A]

  implicit def countedMapEncoder[K: Show, V: Encoder: Count]: Encoder[Map[K, V]] = new Encoder[Map[K, V]] {
    override def apply(map: Map[K, V]): Json = {
      val contents = map.map { case (key, value) =>
        Show[K].show(key) -> Encoder[V].apply(value)
      }.toSeq

      Json.obj("total" -> Json.fromInt(map.counted), "values" -> Json.obj(contents: _*))
    }
  }

  val dsl: Http4sDsl[UserTask] = Http4sDsl[UserTask]

  import dsl._

  def route: HttpRoutes[UserTask] =
    HttpRoutes.of[UserTask] {
      case GET -> Root                    => wordCount.foldM(e => InternalServerError(s"Blackbox failed: ${e.getMessage}"), Ok(_))
      case GET -> Root / eventType        =>
        wordCount
          .map(_.get(eventType))
          .foldM(
            e => InternalServerError(s"Blackbox failed: ${e.getMessage}"),
            {
              case Some(value) => Ok(value)
              case None        => NotFound()
            }
          )
      case GET -> Root / eventType / word =>
        wordCount
          .map(_.get(eventType).flatMap(_.get(word)))
          .foldM(
            e => InternalServerError(s"Blackbox failed: ${e.getMessage}"),
            {
              case Some(value) => Ok(value)
              case None        => NotFound()
            }
          )
    }

}
