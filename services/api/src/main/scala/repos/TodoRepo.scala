package repos

import api.request.AddTodo
import domain.*
import domain.errors.ApiError
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import java.util.UUID

trait TodoRepo {
  def findAllByUserId(userId: Int): Task[List[Todo]]
  def markCompleted(id: Int): Task[Todo]

  def ownedBy(id: Int, userId: User.ID): IO[ApiError, Boolean] =
    get(id).map(_.parentId == userId)

  def get(id: Todo.ID): IO[ApiError, Todo]
  def add(entity: AddTodo, userId: User.ID): Task[Todo]

  def delete(id: Todo.ID): Task[Todo]
}

case class TodoRepoLive(quill: Quill[PostgresDialect, SnakeCase]) extends TodoRepo {
  import quill.*

  inline given InsertMeta[Todo] = insertMeta(_.id)

  override def get(id: Todo.ID): IO[ApiError, Todo] =
    run(query[Todo].filter(_.id == lift(id)))
      .map(_.headOption)
      .some
      .orElseFail(ApiError.NotFound)

  override def add(entity: AddTodo, userId: User.ID): Task[Todo] =
    run(
      quote(
        query[Todo]
          .insertValue(lift(Todo(0, userId, entity.title, entity.content)))
          .returning(r => r)
      )
    )

  override def findAllByUserId(userId: User.ID): Task[List[Todo]] =
    run(
      query[Todo]
        .filter(_.parentId == lift(userId))
    )

  override def markCompleted(id: Todo.ID): Task[Todo] =
    run(
      quote(
        query[Todo]
          .filter(_.id == lift(id))
          .update(_.completed -> true)
          .returning(r => r)
      )
    )

  override def delete(id: Todo.ID): Task[Todo] =
    run(
      quote(
        query[Todo]
          .filter(_.id == lift(id))
          .delete
          .returning(r => r)
      )
    )

}

object TodoRepo {

  val live: URLayer[Quill[PostgresDialect, SnakeCase], TodoRepo] =
    ZLayer.fromFunction(TodoRepoLive.apply)

  def get(id: Int): ZIO[TodoRepo, ApiError, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.get(id))

  def add(todo: AddTodo, userId: User.ID): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.add(todo, userId))

  def markCompleted(id: Int): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.markCompleted(id))

  def findAllByUserId(userId: Int): RIO[TodoRepo, List[Todo]] =
    ZIO.serviceWithZIO[TodoRepo](_.findAllByUserId(userId))

  def ownedBy(id: Int, userId: Int): ZIO[TodoRepo, ApiError, Boolean] =
    ZIO.serviceWithZIO[TodoRepo](_.ownedBy(id, userId))

}
