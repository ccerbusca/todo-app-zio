package services.generators

import com.github.ksuid.{Ksuid, KsuidGenerator}
import zio.*

import java.security.SecureRandom
import java.util.UUID

trait IdGenerator[T] {
  def generate: UIO[T]
}

object IdGenerator {
  val int: ULayer[IdGenerator[Int]] = ZLayer.succeed(IntGenerator())
  val uuid: ULayer[IdGenerator[UUID]] = ZLayer.succeed(UuidGenerator())
  val ksuid: URLayer[SecureRandom, IdGenerator[Ksuid]] = ZLayer.fromFunction(ZKsuidGenerator.apply)
  
  def generate[T: Tag]: RIO[IdGenerator[T], T] =
    ZIO.serviceWithZIO[IdGenerator[T]](_.generate)
}
