package api.services

import api.auth.*
import api.domain.*
import api.errors.ApiError
import api.request.AddTodo
import api.response.TodoResponse
import db.entities.*
import db.repos.TodoRepo
import io.github.arainko.ducktape.*
import todos.todo.ZioTodo.TodoServiceClient
import todos.todo.{AddTodoRequest, Id, Todo as GTodo}
import zio.*
import zio.http.*
import zio.json.*
import zio.stream.ZSink

import java.util.UUID

trait TodoService {
  def get(id: Todo.ID): IO[ApiError, TodoResponse]
  def add(todoDTO: AddTodo, userId: User.ID): IO[ApiError, TodoResponse]
  def allForUser(userId: User.ID): IO[ApiError, List[TodoResponse]]
  def markCompleted(id: Todo.ID): IO[ApiError, TodoResponse]
  def ownedBy(id: Todo.ID, userId: User.ID): IO[ApiError, Boolean]
}

case class TodoServiceLive(todoRepo: TodoRepo) extends TodoService {

  override def get(id: Todo.ID): IO[ApiError, TodoResponse] =
    todoRepo
      .get(id)
      .map(_.to[TodoResponse])

  override def add(todoDTO: AddTodo, userId: User.ID): IO[ApiError, TodoResponse] =
    for {
      todo <- todoRepo
        .add(todoDTO, userId)
        .orDie
    } yield todo.to[TodoResponse]

  override def allForUser(userId: User.ID): IO[ApiError, List[TodoResponse]] =
    todoRepo
      .findAllByUserId(userId)
      .orDie
      .map(_.map(_.to[TodoResponse]))

  override def markCompleted(id: Todo.ID): IO[ApiError, TodoResponse] =
    todoRepo
      .markCompleted(id)
      .orDie
      .map(_.to[TodoResponse])

  override def ownedBy(id: Todo.ID, userId: User.ID): IO[ApiError, Boolean] =
    todoRepo
      .get(id)
      .map(_.parentId == userId)
      .filterOrFail(identity)(ApiError.Unauthorized)

}

case class TodoServiceV2(todoServiceClient: TodoServiceClient) extends TodoService {

  override def get(id: Todo.ID): IO[ApiError, TodoResponse] =
    todoServiceClient
      .getTodo(Id(id))
      .mapBoth(
        _ => ApiError.NotFound,
        _.to[TodoResponse],
      )

  override def add(todoDTO: AddTodo, userId: User.ID): IO[ApiError, TodoResponse] =
    todoServiceClient
      .addTodo(
        AddTodoRequest(
          entity = Some(GTodo(todoDTO.title, todoDTO.content, id = None)),
          userId = userId,
        )
      ).mapBoth(
        _ => ApiError.InternalError,
        _.to[TodoResponse],
      )

  override def allForUser(userId: User.ID): IO[ApiError, List[TodoResponse]] =
    todoServiceClient
      .allForUser(Id(userId))
      .map(_.to[TodoResponse])
      .runCollect
      .mapBoth(
        _ => ApiError.InternalError,
        _.toList,
      )

  override def markCompleted(id: Todo.ID): IO[ApiError, TodoResponse] =
    todoServiceClient
      .markCompleted(Id(id))
      .mapBoth(
        _ => ApiError.InternalError,
        _.to[TodoResponse],
      )

  override def ownedBy(id: Todo.ID, userId: User.ID): IO[ApiError, Boolean] =
    todoServiceClient
      .getTodo(Id(id))
      .mapBoth(
        _ => ApiError.InternalError,
        _.userId == userId,
      )

}

object TodoService {

  val live: URLayer[TodoRepo, TodoService] =
    ZLayer.fromFunction(TodoServiceLive.apply)

  val v2_grpc: URLayer[TodoServiceClient, TodoService] =
    ZLayer.fromFunction(TodoServiceV2.apply)

}
