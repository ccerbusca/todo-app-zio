package unit

import domain.WithId
import domain.errors.ApiError.NotFound
import repos.{InMemoryRepo, InMemoryRepoLive}
import unit.InMemoryRepoSpec.{suite, test}
import zio.ZLayer
import zio.concurrent.ConcurrentMap
import zio.test.Assertion.{equalTo, fails}
import zio.test.{ZIOSpecDefault, assertTrue, assertZIO}

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
          fails(equalTo(NotFound))
        ).provideLayer(ZLayer.fromZIO(ConcurrentMap.empty[Int, TestObject].map(InMemoryRepoLive.apply)))
      },

      test("should update entity correctly") {
        for {
          map <- ConcurrentMap.empty[Int, TestObject]
          repo = InMemoryRepoLive[TestObject, Int](map)
          obj = TestObject("string", 2)
          _ <- repo.add(obj)
          _ <- repo.update(2, _.copy(content = "1234"))
          found <- repo.find(_.id == 2)
        } yield assertTrue(found.content == "1234")
      }
    )

  case class TestObject(content: String, id: Int) extends WithId[Int]
}
