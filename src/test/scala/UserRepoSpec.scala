import domain.User
import domain.dto.UserRegister
import repos.{InMemoryRepo, InMemoryRepoLive}
import repos.user.{UserRepoInMemory, UserRepoLive}
import zio.concurrent.ConcurrentMap
import zio.test.*
import zio.test.Assertion.*
import zio.{Ref, ZIO}

import java.util.UUID

object UserRepoSpec extends ZIOSpecDefault {
  override def spec =
    suite("UserRepoSpec") {
      test("Repo adds user correctly") {
        for {
          map <- ConcurrentMap.empty[UUID, User]
          inMemory: InMemoryRepo[User, UUID] = InMemoryRepoLive[User, UUID](map)
          repo = UserRepoInMemory(inMemory)
          uuid <- zio.Random.nextUUID
          _ <- TestRandom.feedUUIDs(uuid)
          newUser = User("test", "test", uuid)
          _ <- repo.add(newUser)
          user <- repo.get(uuid)
          exists <- map.exists((k, _) => k == uuid)
        } yield assertTrue(User("test", "test", uuid) == user && exists)
      }
    }
}
