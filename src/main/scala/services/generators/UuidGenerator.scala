package services.generators

import zio.*

import java.util.UUID

case class UuidGenerator() extends Generator[UUID] {
  override def generate: UIO[UUID] = Random.nextUUID
}

object UuidGenerator {
  def newUuid: RIO[Generator[UUID], UUID] =
    ZIO.serviceWithZIO[Generator[UUID]](_.generate)
}