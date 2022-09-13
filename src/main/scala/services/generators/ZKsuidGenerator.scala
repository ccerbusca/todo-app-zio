package services.generators

import com.github.ksuid.{Ksuid, KsuidGenerator}
import zio.*

import java.security.SecureRandom

case class ZKsuidGenerator(secureRandom: SecureRandom) extends Generator[Ksuid] {
  private val generator = new KsuidGenerator(secureRandom)

  override def generate: UIO[Ksuid] = ZIO.succeed(generator.newKsuid())
}

object ZKsuidGenerator {
  def newKsuid: RIO[Generator[Ksuid], Ksuid] =
    ZIO.serviceWithZIO[Generator[Ksuid]](_.generate)
}
