package functional

import domain.User
import io.getquill.*
import io.getquill.jdbczio.Quill
import io.getquill.jdbczio.Quill.Postgres
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import repos.user.UserRepo
import services.generators.IdGenerator
import testinstances.UserGenerator
import unit.InMemoryRepoSpec.TestObject
import zio.*
import zio.test.*

object UserRepoLiveSpec extends ZIOSpecDefault {
  override def spec =
    (suite("UserRepoLiveSpec")(

      test("User should be correctly added") {
        for {
          user <- UserGenerator.generate
          inserted <- UserRepo.add(user)
        } yield assertTrue(user == inserted)
      },

      test("User should be correctly fetched by id") {
        for {
          inserted <- UserGenerator.generate.flatMap(UserRepo.add)
          fetched <- UserRepo.get(inserted.id)
        } yield assertTrue(fetched == inserted)
      },

      test("User should be correctly fetched by username") {
        for {
          inserted <- UserGenerator.generate.flatMap(UserRepo.add)
          fetched <- UserRepo.findByUsername(inserted.username)
        } yield assertTrue(fetched == inserted)
      }



    ) @@ DbMigrationAspect.migrate()())
      .provide(
        IdGenerator.int(),
        UserGenerator.instance,
        UserRepo.live,
        ZPostgreSQLContainer.Settings.default,
        ZPostgreSQLContainer.live,
        Quill.Postgres.fromNamingStrategy(SnakeCase),
      )
}
