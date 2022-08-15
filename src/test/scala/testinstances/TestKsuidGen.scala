package testinstances

import auth.ZKsuidGenerator
import com.github.ksuid.{Ksuid, KsuidGenerator}
import zio.*

import java.security.SecureRandom

trait TestKsuidGen extends ZKsuidGenerator {
  def feedKsuids(ksuids: Ksuid*): UIO[Unit]
}

case class TestKsuidGenerator(secureRandom: SecureRandom, buffer: Ref[List[Ksuid]]) extends TestKsuidGen {
  private val generator = new KsuidGenerator(secureRandom)

  override def newKsuid: UIO[Ksuid] =
    buffer.getAndUpdate {
      case _ :: tail => tail
      case Nil => Nil
    }.map(_.headOption).some <> ZIO.succeed(generator.newKsuid())

  override def feedKsuids(ksuids: Ksuid*): UIO[Unit] =
    buffer.update(_ ++ ksuids.toList)
}

object TestKsuidGen {
  val test: URLayer[SecureRandom, TestKsuidGen] =
    ZLayer.fromZIO {
      for {
        ref <- Ref.make(List.empty[Ksuid])
        random <- ZIO.service[SecureRandom]
      } yield TestKsuidGenerator(random, ref)
    }

  def feedKsuids(ksuids: Ksuid*): RIO[TestKsuidGen, Unit] =
    ZIO.serviceWithZIO[TestKsuidGen](_.feedKsuids(ksuids: _*))
}
