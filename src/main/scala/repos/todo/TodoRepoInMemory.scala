package repos.todo

import domain.Todo
import domain.dto.AddTodo
import repos.InMemoryRepo
import zio.{Random, Task}

import java.util.UUID

case class TodoRepoInMemory(inMemoryRepo: InMemoryRepo[Todo, Int]) extends TodoRepo {
  override def get(id: Int): Task[Todo] =
    inMemoryRepo.get(id)

  override def add(todo: Todo): Task[Todo] =
    inMemoryRepo.add(todo)

  override def findAllByParentId(parentId: Int): Task[List[Todo]] =
    inMemoryRepo.filter(_.parentId == parentId)
}
