package asavelyev

package object wordcounter {
  type EventType = String
  type Word      = String
  type WordCount = Map[EventType, Map[Word, Int]]
  object WordCount {
    val empty: WordCount                                 = Map.empty
    def one(eventType: EventType, word: Word): WordCount = Map(eventType -> Map(word -> 1))
  }
}
