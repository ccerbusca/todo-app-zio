package services.repos.todo

import domain.Todo
import domain.dto.AddTodo
import services.repos.{InMemoryRepo, Repo}
import zhttp.*
import zhttp.http.*
import zhttp.service.*
import zio.*
import zio.json.*

import java.util.UUID

trait TodoRepo extends Repo[Todo, UUID] {
  def add(addDTO: AddTodo): Task[Todo]
}

object TodoRepo {
  val inMemory: URLayer[InMemoryRepo[Todo, UUID], TodoRepoInMemory] =
    ZLayer {
      ZIO.service[InMemoryRepo[Todo, UUID]].map(TodoRepoInMemory.apply)
    }

  def get(id: UUID): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.get(id))

  def find(pred: Todo => Boolean): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.find(pred))

  def add(addDTO: AddTodo): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.add(addDTO))

  val endpoints: HttpApp[TodoRepo, Throwable] = Http.collectZIO[Request] {
    case req @ Method.POST -> !! / "todo" =>
      for {
        body <- req.bodyAsString
        todoDTO <- ZIO.fromEither(body.fromJson[AddTodo])
          .mapError(new RuntimeException(_))
        todo <- TodoRepo.add(todoDTO)
      } yield Response.json(todo.toJson)
  }
}
