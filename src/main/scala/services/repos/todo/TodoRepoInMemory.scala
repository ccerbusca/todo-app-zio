package services.repos.todo

import domain.Todo
import domain.dto.AddTodo
import services.repos.InMemoryRepo
import zio.{Random, Task}

import java.util.UUID

case class TodoRepoInMemory(inMemoryRepo: InMemoryRepo[Todo, UUID]) extends TodoRepo {
  override def get(id: UUID): Task[Todo] =
    inMemoryRepo.get(id)

  override def find(pred: Todo => Boolean): Task[Todo] =
    inMemoryRepo.find(pred)

  override def add(addDTO: AddTodo): Task[Todo] =
    for {
      uuid <- Random.nextUUID
      newTodo = Todo(addDTO.title, addDTO.content, uuid)
      todo <- inMemoryRepo.add(newTodo)
    } yield todo
}
