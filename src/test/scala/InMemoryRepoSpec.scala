import domain.{User, WithId}
import services.repos.{InMemoryRepo, InMemoryRepoLive}
import zio.test.*
import zio.test.Assertion.*
import zio.*
import domain.errors.CustomError.NotFound
import zio.concurrent.ConcurrentMap

import java.util.UUID

object InMemoryRepoSpec extends ZIOSpecDefault {
  override def spec =
    suite("InMemoryRepoSpec")(
      test("should add an entity correctly") {
        for {
          map <- ConcurrentMap.empty[Int, TestObject]
          repo = InMemoryRepoLive[TestObject, Int](map)
          obj = TestObject("string", 2)
          _ <- repo.add(obj)
          addedObj <- repo.get(2)
        } yield assertTrue(obj == addedObj)
      },

      test("should find an entity correctly") {
        for {
          map <- ConcurrentMap.empty[Int, TestObject]
          repo = InMemoryRepoLive[TestObject, Int](map)
          obj = TestObject("string", 2)
          _ <- repo.add(obj)
          foundObj <- repo.find(_.content == "string")
        } yield assertTrue(foundObj == obj)
      },

      test("effect should fail if an entity was not found") {
        assertZIO(
          InMemoryRepo.find[TestObject, Int](_.content == "wrong").exit
        )(
          fails(equalTo(NotFound()))
        ).provideLayer(ZLayer.fromZIO(ConcurrentMap.empty[Int, TestObject].map(InMemoryRepoLive.apply)))
      }
    )

  case class TestObject(content: String, id: Int) extends WithId[Int]
}
