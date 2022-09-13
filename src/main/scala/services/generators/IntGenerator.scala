package services.generators

import zio.*

case class IntGenerator(ref: Ref[Int]) extends IdGenerator[Int] {
  override def generate: UIO[Int] =
    ref.getAndUpdate(_ + 1)
}

object IntGenerator {
  def live(start: Int = 1): ULayer[IntGenerator] = ZLayer {
    Ref.make(start).map(IntGenerator.apply)
  }
  def newInt: RIO[IdGenerator[Int], Int] =
    ZIO.serviceWithZIO[IdGenerator[Int]](_.generate)
}
