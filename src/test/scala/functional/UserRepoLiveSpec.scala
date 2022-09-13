package functional

import domain.User
import io.getquill.*
import io.getquill.jdbczio.Quill
import io.getquill.jdbczio.Quill.Postgres
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import repos.user.UserRepo
import unit.InMemoryRepoSpec.TestObject
import zio.*
import zio.test.*

object UserRepoLiveSpec extends ZIOSpecDefault {
  override def spec =
    (suite("UserRepoLiveSpec")(

      test("User should be correctly added") {
        for {
          inserted <- UserRepo.add(User("123", "456", 1))
        } yield assertTrue(User("123", "456", 1) == inserted)
      },

      test("User should be correctly fetched by id") {
        for {
          inserted <- UserRepo.add(User("123", "456", 2))
          fetched <- UserRepo.get(2)
        } yield assertTrue(fetched == inserted)
      },

      test("User should be correctly fetched by username") {
        for {
          inserted <- UserRepo.add(User("username", "456", 3))
          fetched <- UserRepo.findByUsername("username")
        } yield assertTrue(fetched == inserted)
      }



    ) @@ DbMigrationAspect.migrate()())
      .provide(
        UserRepo.live,
        ZPostgreSQLContainer.Settings.default,
        ZPostgreSQLContainer.live,
        Quill.Postgres.fromNamingStrategy(SnakeCase),
      )
}
