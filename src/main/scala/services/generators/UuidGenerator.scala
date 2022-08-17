package services.generators

import zio.*

import java.util.UUID

case class UuidGenerator() extends IdGenerator[UUID] {
  override def generate: UIO[UUID] = Random.nextUUID
}

object UuidGenerator {
  def newUuid: RIO[IdGenerator[UUID], UUID] =
    ZIO.serviceWithZIO[IdGenerator[UUID]](_.generate)
}