package auth

import com.github.ksuid.{Ksuid, KsuidGenerator}
import zio.*

import java.security.SecureRandom

trait ZKsuidGenerator {
  def newKsuid: UIO[Ksuid]
}

case class ZKsuidGeneratorLive(secureRandom: SecureRandom) extends ZKsuidGenerator {
  private val generator = new KsuidGenerator(secureRandom)

  override def newKsuid: UIO[Ksuid] = ZIO.succeed(generator.newKsuid())
}

object ZKsuidGenerator {
  val live: URLayer[SecureRandom, ZKsuidGenerator] = ZLayer.fromFunction(ZKsuidGeneratorLive.apply)

  def newKsuid: RIO[ZKsuidGenerator, Ksuid] =
    ZIO.serviceWithZIO[ZKsuidGenerator](_.newKsuid)
}
