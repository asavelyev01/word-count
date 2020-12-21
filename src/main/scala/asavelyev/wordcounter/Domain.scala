package asavelyev.wordcounter

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class BlackboxLine(event_type: EventType, data: Word)

object BlackboxLine {
  implicit val decoder: Decoder[BlackboxLine] = deriveDecoder[BlackboxLine]
}
