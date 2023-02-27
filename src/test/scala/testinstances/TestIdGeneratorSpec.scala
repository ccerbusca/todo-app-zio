package testinstances

import com.github.ksuid.Ksuid
import domain.generators.{ Generator, ZKsuidGenerator }
import zio.*
import zio.test.*

import java.security.SecureRandom

object TestIdGeneratorSpec extends ZIOSpecDefault {

  override def spec =
    suite("TestKsuidGenSpec")(
      test("fed Ksuids should be provided instead of randomly generated ones") {
        for {
          ksuid        <- ZKsuidGenerator.newKsuid
          ksuid2       <- ZKsuidGenerator.newKsuid
          _            <- TestGenerator.feedIds(ksuid, ksuid2)
          newKsuid     <- ZKsuidGenerator.newKsuid
          newKsuid2    <- ZKsuidGenerator.newKsuid
          anotherKsuid <- ZKsuidGenerator.newKsuid
        } yield assertTrue(
          ksuid == newKsuid && ksuid2 == newKsuid2 && newKsuid != anotherKsuid && newKsuid2 != anotherKsuid
        )
      },
      test("if no ksuids are fed, generate different ksuids") {
        for {
          ksuid    <- ZKsuidGenerator.newKsuid
          newKsuid <- ZKsuidGenerator.newKsuid
        } yield assertTrue(ksuid != newKsuid)
      },
    ).provide(
      // Preserve this order to prevent circular dependency error (TestIdGenerator.test[Ksuid] needs IdGenerator.ksuid, while itself being a Ksuid generator)
      Generator.ksuid,
      TestGenerator.test[Ksuid],
      ZLayer.succeed(new SecureRandom()),
    )

}
