import domain.User
import domain.dto.UserRegistrationDTO
import services.repos.user.UserRepoLive
import zio.test.*
import zio.test.Assertion.*
import zio.{Ref, ZIO}

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
          newUser = UserRegistrationDTO("test", "test")
          _ <- repo.add(newUser)
          map <- db.get
          user <- repo.get(uuid)
        } yield assertTrue(User("test", "test", uuid) == user && map.contains(uuid))
      }
    }
}
