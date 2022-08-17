package services.generators

import zio.*

case class IntGenerator() extends IdGenerator[Int] {
  override def generate: UIO[Int] = Random.nextInt
}

object IntGenerator {
  def newInt: RIO[IdGenerator[Int], Int] =
    ZIO.serviceWithZIO[IdGenerator[Int]](_.generate)
}
