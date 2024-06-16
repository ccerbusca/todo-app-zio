package utils.testinstances

import com.github.ksuid.{ Ksuid, KsuidGenerator }
import zio.*

import java.security.SecureRandom
import java.util.UUID

trait Generator[T] {
  def generate: UIO[T]
}

object Generator {
  def int(start: Int = 1): ULayer[Generator[Int]]    = IntGenerator.live(start)
  val uuid: ULayer[Generator[UUID]]                  = ZLayer.succeed(UuidGenerator())
  val ksuid: URLayer[SecureRandom, Generator[Ksuid]] = ZLayer.fromFunction(ZKsuidGenerator.apply)

  def generate[T: Tag]: RIO[Generator[T], T] =
    ZIO.serviceWithZIO[Generator[T]](_.generate)

}
