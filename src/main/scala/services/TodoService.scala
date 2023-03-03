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
  def add(todoDTO: AddTodo, userId: User.ID): IO[ApiError, TodoResponse]
  def allForUser(userId: User.ID): IO[ApiError, List[TodoResponse]]
  def markCompleted(id: Int): IO[ApiError, TodoResponse]
  def ownedBy(id: Int, userId: User.ID): IO[ApiError, Boolean]
}

case class TodoServiceLive(todoRepo: TodoRepo) extends TodoService {

  override def get(id: Int): IO[ApiError, TodoResponse] =
    todoRepo
      .get(id)
      .map(_.to[TodoResponse])

  override def add(todoDTO: AddTodo, userId: User.ID): IO[ApiError, TodoResponse] =
    for {
      todo <- todoRepo.add(todoDTO, userId)
    } yield todo.to[TodoResponse]

  override def allForUser(userId: User.ID): IO[ApiError, List[TodoResponse]] =
    todoRepo
      .findAllByUserId(userId)
      .map(_.map(_.to[TodoResponse]))

  override def markCompleted(id: Int): IO[ApiError, TodoResponse] =
    todoRepo
      .markCompleted(id)
      .map(_.to[TodoResponse])

  override def ownedBy(id: Int, userId: User.ID): IO[ApiError, Boolean] =
    todoRepo
      .ownedBy(id, userId)
      .filterOrFail(identity)(Unauthorized)

}

object TodoService {

  val live: URLayer[TodoRepo, TodoService] =
    ZLayer.fromFunction(TodoServiceLive.apply)

}
