package services

import auth.*
import domain.api.request.AddTodo
import domain.api.response.TodoResponse
import domain.errors.ApiError
import domain.errors.ApiError.Unauthorized
import domain.generators.Generator
import domain.{Todo, User}
import io.github.arainko.ducktape.*
import repos.TodoRepo
import zio.*
import zio.http.*
import zio.http.model.*
import zio.json.*

import java.util.UUID

trait TodoService {
  def get(id: Int): IO[ApiError, TodoResponse]
  def add(todoDTO: AddTodo, user: User): IO[ApiError, TodoResponse]
  def allForUser(user: User): IO[ApiError, List[TodoResponse]]
  def markCompleted(id: Int): IO[ApiError, TodoResponse]
  def ownedBy(id: Int, user: User): IO[ApiError, Boolean]
}

case class TodoServiceLive(todoRepo: TodoRepo, idGenerator: Generator[Int]) extends TodoService {

  override def get(id: Int): IO[ApiError, TodoResponse] =
    todoRepo
      .get(id)
      .map(_.to[TodoResponse])

  override def add(todoDTO: AddTodo, user: User): IO[ApiError, TodoResponse] =
    for {
      id <- idGenerator.generate
      newTodo = Todo(id, user.id, todoDTO.title, todoDTO.content)
      todo <- todoRepo.add(newTodo)
    } yield todo.to[TodoResponse]

  override def allForUser(user: User): IO[ApiError, List[TodoResponse]] =
    todoRepo
      .findAllByUserId(user.id)
      .map(_.map(_.to[TodoResponse]))

  override def markCompleted(id: Int): IO[ApiError, TodoResponse] =
    todoRepo
      .markCompleted(id)
      .map(_.to[TodoResponse])

  override def ownedBy(id: Int, user: User): IO[ApiError, Boolean] =
    todoRepo
      .ownedBy(id, user.id)
      .filterOrFail(identity)(Unauthorized)

}

object TodoService {

  val live: URLayer[TodoRepo & Generator[Int], TodoService] =
    ZLayer.fromFunction(TodoServiceLive.apply)

  def add(todoDTO: AddTodo, user: User): ZIO[TodoService, ApiError, TodoResponse] =
    ZIO.serviceWithZIO(_.add(todoDTO, user))

  def get(id: Int): ZIO[TodoService, ApiError, TodoResponse] =
    ZIO.serviceWithZIO(_.get(id))

  def allForUser(user: User): ZIO[TodoService, ApiError, List[TodoResponse]] =
    ZIO.serviceWithZIO(_.allForUser(user))

  def markCompleted(id: Int): ZIO[TodoService, ApiError, TodoResponse] =
    ZIO.serviceWithZIO(_.markCompleted(id))

  def ownedBy(id: Int, user: User): ZIO[TodoService, ApiError, Boolean] =
    ZIO.serviceWithZIO[TodoService](_.ownedBy(id, user))

}
