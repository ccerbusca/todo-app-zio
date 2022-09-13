package unit

import domain.User
import repos.user.UserRepoInMemory
import repos.{InMemoryRepo, InMemoryRepoLive}
import unit.UserRepoSpec.{suite, test}
import zio.Random
import zio.concurrent.ConcurrentMap
import zio.test.{ZIOSpecDefault, assertTrue}

object UserRepoSpec extends ZIOSpecDefault {
  override def spec =
    suite("UserRepoSpec") {
      test("Repo adds user correctly") {
        for {
          map <- ConcurrentMap.empty[Int, User]
          inMemory: InMemoryRepo[User, Int] = InMemoryRepoLive[User, Int](map)
          repo = UserRepoInMemory(inMemory)

          id <- Random.nextInt
          newUser = User("test", "test", id)
          _ <- repo.add(newUser)

          user <- repo.get(id)
          exists <- map.exists((k, _) => k == id)
        } yield assertTrue(User("test", "test", id) == user && exists)
      }
    }
}
