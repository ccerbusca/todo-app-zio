package services

import auth.AuthContext
import domain.{Todo, User}
import domain.dto.AddTodo
import repos.todo.TodoRepo
import zhttp.http.*
import zio.*
import zio.json.*
import auth.*
import services.generators.IdGenerator

import java.util.UUID

trait TodoService {
  def get(id: Int): Task[Todo]
  def add(todoDTO: AddTodo, user: User): Task[Todo]

  def allForUser(user: User): Task[List[Todo]]
}

case class TodoServiceLive(todoRepo: TodoRepo, idGenerator: IdGenerator[Int]) extends TodoService {
  override def get(id: Int): Task[Todo] =
    todoRepo.get(id)

  override def add(todoDTO: AddTodo, user: User): Task[Todo] =
    for {
      id <- idGenerator.generate
      newTodo = Todo(todoDTO.title, todoDTO.content, id, user.id)
      todo <- todoRepo.add(newTodo)
    } yield todo

  override def allForUser(user: User): Task[List[Todo]] =
    todoRepo.findAllByParentId(user.id)
}

object TodoService {
  val live: URLayer[TodoRepo & IdGenerator[Int], TodoService] =
    ZLayer.fromFunction(TodoServiceLive.apply)

  def add(todoDTO: AddTodo, user: User): RIO[TodoService, Todo] =
    ZIO.serviceWithZIO[TodoService](_.add(todoDTO, user))

  def get(id: Int): RIO[TodoService, Todo] =
    ZIO.serviceWithZIO[TodoService](_.get(id))

  def allForUser(user: User): RIO[TodoService, List[Todo]] =
    ZIO.serviceWithZIO[TodoService](_.allForUser(user))

  val secureEndpoints: Http[TodoService, Throwable, AuthContext[User], Response] = Http.collectZIO {
    case (req@Method.POST -> !! / "todo") $$ user =>
      for {
        body <- req.bodyAsString
        todoDTO <- ZIO.fromEither(body.fromJson[AddTodo])
          .mapError(new RuntimeException(_))
        todo <- TodoService.add(todoDTO, user)
      } yield Response.json(todo.toJson)
    case Method.POST -> !! / "todos" $$ user =>
      TodoService
        .allForUser(user)
        .map(todos => Response.json(todos.toJson))
  }
}