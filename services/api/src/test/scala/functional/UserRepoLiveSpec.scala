package functional

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.getquill.jdbczio.Quill.Postgres
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import repos.UserRepo
import utils.testinstances.UserRegisterGenerator
import zio.*
import zio.test.*

object UserRepoLiveSpec extends ZIOSpecDefault {

  override def spec: Spec[Any, Any] =
    (suite("UserRepoLiveSpec")(
      test("User should be correctly added and fetched") {
        for {
          user     <- UserRegisterGenerator.generate
          inserted <- UserRepo.add(user)
          fetched  <- UserRepo.get(inserted.id)
        } yield assertTrue(inserted == fetched)
      },
      test("User should be correctly fetched by username") {
        for {
          inserted <- UserRegisterGenerator.generate.flatMap(UserRepo.add)
          fetched  <- UserRepo.findByUsername(inserted.username)
        } yield assertTrue(fetched == inserted)
      },
    ) @@ DbMigrationAspect.migrate()())
      .provide(
        UserRegisterGenerator.instance,
        UserRepo.live,
        ZPostgreSQLContainer.Settings.default,
        ZPostgreSQLContainer.live,
        Quill.Postgres.fromNamingStrategy(SnakeCase),
      )

}
