package repos

import domain.Todo
import domain.api.request.AddTodo
import domain.errors.ApiError
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import java.util.UUID

trait TodoRepo {
  def findAllByUserId(userId: Int): IO[ApiError, List[Todo]]
  def markCompleted(id: Int): IO[ApiError, Todo]

  def ownedBy(id: Int, userId: Int): IO[ApiError, Boolean] =
    get(id).map(_.parentId == userId)

  def get(id: Todo.ID): IO[ApiError, Todo]
  def add(entity: Todo): IO[ApiError, Todo]
}

case class TodoRepoLive(quill: Quill[PostgresDialect, SnakeCase]) extends TodoRepo {
  import quill.*

  override def get(id: Int): IO[ApiError, Todo] =
    run(query[Todo].filter(_.id == lift(id)))
      .map(_.headOption)
      .some
      .orElseFail(ApiError.NotFound)

  override def add(entity: Todo): IO[ApiError, Todo] =
    run(
      quote(
        query[Todo]
          .insertValue(lift(entity))
          .returning(r => r)
      )
    )
      .orElseFail(ApiError.FailedInsert)

  override def findAllByUserId(userId: Int): IO[ApiError, List[Todo]] =
    run(
      query[Todo]
        .filter(_.parentId == lift(userId))
    )
      .orElseFail(ApiError.NotFound)

  override def markCompleted(id: Int): IO[ApiError, Todo] =
    run(
      quote(
        query[Todo]
          .filter(_.id == lift(id))
          .update(_.completed -> true)
          .returning(r => r)
      )
    )
      .orElseFail(ApiError.FailedInsert)

}

object TodoRepo {

  val live: URLayer[Quill[PostgresDialect, SnakeCase], TodoRepo] =
    ZLayer.fromFunction(TodoRepoLive.apply)

  def get(id: Int): ZIO[TodoRepo, ApiError, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.get(id))

  def add(todo: Todo): ZIO[TodoRepo, ApiError, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.add(todo))

  def markCompleted(id: Int): ZIO[TodoRepo, ApiError, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.markCompleted(id))

  def findAllByUserId(userId: Int): ZIO[TodoRepo, ApiError, List[Todo]] =
    ZIO.serviceWithZIO[TodoRepo](_.findAllByUserId(userId))

  def ownedBy(id: Int, userId: Int): ZIO[TodoRepo, ApiError, Boolean] =
    ZIO.serviceWithZIO[TodoRepo](_.ownedBy(id, userId))

}
