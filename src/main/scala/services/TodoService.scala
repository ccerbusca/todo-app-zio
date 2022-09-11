package services

import auth.*
import domain.dto.request.AddTodo
import domain.{Todo, User}
import repos.todo.TodoRepo
import services.generators.IdGenerator
import zhttp.http.*
import zhttp.*
import zio.*
import zio.json.*

import java.util.UUID

trait TodoService {
  def get(id: Int): Task[Todo]
  def add(todoDTO: AddTodo, user: User): Task[Todo]
  def allForUser(user: User): Task[List[Todo]]
  def markCompleted(id: Int): Task[Todo]
}

case class TodoServiceLive(todoRepo: TodoRepo, idGenerator: IdGenerator[Int]) extends TodoService {
  override def get(id: Int): Task[Todo] =
    todoRepo.get(id)

  override def add(todoDTO: AddTodo, user: User): Task[Todo] =
    for {
      id <- idGenerator.generate
      newTodo = Todo(id, user.id, todoDTO.title, todoDTO.content)
      todo <- todoRepo.add(newTodo)
    } yield todo

  override def allForUser(user: User): Task[List[Todo]] =
    todoRepo.findAllByParentId(user.id)

  override def markCompleted(id: Int): Task[Todo] =
    todoRepo.markCompleted(id)

}

object TodoService {
  val live: URLayer[TodoRepo & IdGenerator[Int], TodoService] =
    ZLayer.fromFunction(TodoServiceLive.apply)

  def add(todoDTO: AddTodo, user: User): RIO[TodoService, Todo] =
    ZIO.serviceWithZIO(_.add(todoDTO, user))

  def get(id: Int): RIO[TodoService, Todo] =
    ZIO.serviceWithZIO(_.get(id))

  def allForUser(user: User): RIO[TodoService, List[Todo]] =
    ZIO.serviceWithZIO(_.allForUser(user))

  def markCompleted(id: Int): RIO[TodoService, Todo] =
    ZIO.serviceWithZIO(_.markCompleted(id))

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
    case Method.POST -> !! / "todo" / int(id) $$ user =>
      TodoService
        .markCompleted(id)
        .map(entity => Response.json(entity.toJson))
  }
}