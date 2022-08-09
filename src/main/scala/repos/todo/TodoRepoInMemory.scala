package repos.todo

import domain.Todo
import domain.dto.AddTodo
import repos.InMemoryRepo
import zio.{Random, Task}

import java.util.UUID

case class TodoRepoInMemory(inMemoryRepo: InMemoryRepo[Todo, UUID]) extends TodoRepo {
  override def get(id: UUID): Task[Todo] =
    inMemoryRepo.get(id)

  override def add(todo: Todo): Task[Todo] =
    inMemoryRepo.add(todo)
}
