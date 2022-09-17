package services

import auth.*
import domain.dto.request.AddTodo
import domain.errors.ApiError.Unauthorized
import domain.{Todo, User}
import repos.todo.TodoRepo
import services.generators.Generator
import zhttp.*
import zhttp.http.*
import zio.*
import zio.json.*

import java.util.UUID

trait TodoService {
  def get(id: Int): Task[Todo]
  def add(todoDTO: AddTodo, user: User): Task[Todo]
  def allForUser(user: User): Task[List[Todo]]
  def markCompleted(id: Int): Task[Todo]
  def ownedBy(id: Int, user: User): Task[Unit]
}

case class TodoServiceLive(todoRepo: TodoRepo, idGenerator: Generator[Int]) extends TodoService {
  override def get(id: Int): Task[Todo] =
    todoRepo.get(id)

  override def add(todoDTO: AddTodo, user: User): Task[Todo] =
    for {
      id <- idGenerator.generate
      newTodo = Todo(id, user.id, todoDTO.title, todoDTO.content)
      todo <- todoRepo.add(newTodo)
    } yield todo

  override def allForUser(user: User): Task[List[Todo]] =
    todoRepo.findAllByUserId(user.id)

  override def markCompleted(id: Int): Task[Todo] =
    todoRepo.markCompleted(id)

  override def ownedBy(id: Int, user: User): Task[Unit] =
    todoRepo
      .ownedBy(id, user.id)
      .flatMap(if (_) ZIO.succeed(()) else ZIO.fail(Unauthorized))
}

object TodoService {
  val live: URLayer[TodoRepo & Generator[Int], TodoService] =
    ZLayer.fromFunction(TodoServiceLive.apply)

  def add(todoDTO: AddTodo, user: User): RIO[TodoService, Todo] =
    ZIO.serviceWithZIO(_.add(todoDTO, user))

  def get(id: Int): RIO[TodoService, Todo] =
    ZIO.serviceWithZIO(_.get(id))

  def allForUser(user: User): RIO[TodoService, List[Todo]] =
    ZIO.serviceWithZIO(_.allForUser(user))

  def markCompleted(id: Int): RIO[TodoService, Todo] =
    ZIO.serviceWithZIO(_.markCompleted(id))

  def ownedBy(id: Int, user: User): RIO[TodoService, Unit] =
    ZIO.serviceWithZIO[TodoService](_.ownedBy(id, user))

  val secureEndpoints: Http[TodoService, Throwable, AuthContext[User], Response] = Http.collectZIO {
    case (req@Method.POST -> !! / "todo") $$ user =>
      for {
        body <- req.body.asString
        todoDTO <- ZIO.fromEither(body.fromJson[AddTodo])
          .mapError(new RuntimeException(_))
        todo <- TodoService.add(todoDTO, user)
      } yield Response.json(todo.toJson)
    case Method.POST -> !! / "todos" $$ user =>
      TodoService
        .allForUser(user)
        .map(todos => Response.json(todos.toJson))
    case Method.POST -> !! / "todo" / int(id) $$ user =>
      TodoService.ownedBy(id, user) *>
        TodoService
          .markCompleted(id)
          .map(entity => Response.json(entity.toJson))
    case Method.GET -> !! / "todo" / int(id) $$ user =>
      TodoService.ownedBy(id, user) *>
        TodoService
          .get(id)
          .map(entity => Response.json(entity.toJson))
  }
}