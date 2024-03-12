package functional

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.getquill.jdbczio.Quill.Postgres
import repos.{UserRepo, db}
import utils.testinstances.UserRegisterGenerator
import zio.*
import zio.test.*

object UserRepoLiveSpec extends BaseFunctionalTest {

  override def tests: Spec[db.QuillPostgres, Any] =
    suite("UserRepoLiveSpec")(
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
    )
      .provideSome[db.QuillPostgres](
        UserRegisterGenerator.instance,
        UserRepo.live,
      )

}
