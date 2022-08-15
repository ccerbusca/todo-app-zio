package testinstances

import auth.ZKsuidGenerator
import zio.test.*
import zio.*

import java.security.SecureRandom

object TestKsuidGenSpec extends ZIOSpecDefault {

  override def spec =
    suite("TestKsuidGenSpec")(
      test("fed Ksuid should be provided instead of a randomly generated one") {
        for {
          ksuid <- ZKsuidGenerator.newKsuid
          _ <- TestKsuidGen.feedKsuids(ksuid)
          newKsuid <- ZKsuidGenerator.newKsuid
          anotherKsuid <- ZKsuidGenerator.newKsuid
        } yield assertTrue(ksuid == newKsuid && newKsuid != anotherKsuid)
      },

      test("if no ksuids are fed, generate different ksuids") {
        for {
          ksuid <- ZKsuidGenerator.newKsuid
          newKsuid <- ZKsuidGenerator.newKsuid
        } yield assertTrue(ksuid != newKsuid)
      }
    ).provide(
      TestKsuidGen.test,
      ZLayer.succeed(new SecureRandom())
    )

}
