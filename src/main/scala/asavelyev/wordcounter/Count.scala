package asavelyev.wordcounter

trait Count[T] {
  def count(t: T): Int
}

object Count {
  implicit def intCount = new Count[Int] {
    override def count(t: Int): Int = t
  }

  implicit def mapCount[K, V: Count] = new Count[Map[K, V]] {
    override def count(t: Map[K, V]): Int = t.values.map(_.counted).sum
  }

  implicit class Syntax[T](val self: T) extends AnyVal {
    def counted(implicit count: Count[T]) = count.count(self)
  }
}
