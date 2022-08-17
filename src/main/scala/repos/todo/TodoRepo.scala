package repos.todo

import domain.Todo
import domain.dto.AddTodo
import repos.{InMemoryRepo, Repo}
import zio.*

import java.util.UUID

trait TodoRepo extends Repo[Todo, Int] {
  def findAllByParentId(parentId: Int): Task[List[Todo]]
}

object TodoRepo {
  val inMemory: URLayer[InMemoryRepo[Todo, Int], TodoRepoInMemory] =
    ZLayer.fromFunction(TodoRepoInMemory.apply)

  def get(id: Int): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.get(id))

  def add(todo: Todo): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.add(todo))
}
