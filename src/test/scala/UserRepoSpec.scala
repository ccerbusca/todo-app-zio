import domain.User
import services.repos.UserRepoLive
import zio.{Ref, ZIO}
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID

object UserRepoSpec extends ZIOSpecDefault {
  override def spec =
    suite("UserRepoSpec") {
      test("Repo adds user correctly") {
        for {
          db <- Ref.make(Map.empty[UUID, User])
          repo = UserRepoLive(db)
          uuid <- zio.Random.nextUUID
          _ <- TestRandom.feedUUIDs(uuid)
          newUser = User("test", "test")
          _ <- repo.add(newUser)
          map <- db.get
          user <- repo.get(uuid)
        } yield assertTrue(newUser == user.entity && map.contains(uuid))
      }
    }
}
