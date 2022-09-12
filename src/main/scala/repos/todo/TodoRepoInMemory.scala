package repos.todo

import domain.Todo
import domain.dto.request.AddTodo
import repos.InMemoryRepo
import zio.{Random, Task}

import java.util.UUID

case class TodoRepoInMemory(inMemoryRepo: InMemoryRepo[Todo, Int]) extends TodoRepo {
  override def get(id: Int): Task[Todo] =
    inMemoryRepo.get(id)

  override def add(todo: Todo): Task[Todo] =
    inMemoryRepo.add(todo)

  override def findAllByUserId(userId: Int): Task[List[Todo]] =
    inMemoryRepo.filter(_.parentId == userId)

  override def markCompleted(id: Int): Task[Todo] =
    inMemoryRepo.update(id, _.copy(completed = true))
}
