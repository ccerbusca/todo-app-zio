package db.repos

import api.errors.ApiError
import api.request.AddTodo
import db.entities.*
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

trait TodoRepo {
  def findAllByUserId(userId: User.ID): Task[List[Todo]]
  def markCompleted(id: Todo.ID): Task[Todo]

  def ownedBy(id: Todo.ID, userId: User.ID): IO[ApiError, Boolean] =
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

  def get(id: Todo.ID): ZIO[TodoRepo, ApiError, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.get(id))

  def add(todo: AddTodo, userId: User.ID): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.add(todo, userId))

  def markCompleted(id: Todo.ID): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.markCompleted(id))

  def findAllByUserId(userId: User.ID): RIO[TodoRepo, List[Todo]] =
    ZIO.serviceWithZIO[TodoRepo](_.findAllByUserId(userId))

  def ownedBy(id: Todo.ID, userId: User.ID): ZIO[TodoRepo, ApiError, Boolean] =
    ZIO.serviceWithZIO[TodoRepo](_.ownedBy(id, userId))

}
