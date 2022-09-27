package repos

import domain.Todo
import domain.api.request.AddTodo
import domain.errors.ApiError.NotFound
import io.getquill.*
import io.getquill.jdbczio.Quill
import repos.Repo
import zio.*

import java.util.UUID

trait TodoRepo extends Repo[Todo, Int] {
  def findAllByUserId(userId: Int): Task[List[Todo]]
  def markCompleted(id: Int): Task[Todo]
  def ownedBy(id: Int, userId: Int): Task[Boolean] =
    get(id).map(_.parentId == userId)
}

case class TodoRepoLive(quill: Quill[PostgresDialect, SnakeCase]) extends TodoRepo {
  import quill.*

  override def get(id: Int): Task[Todo] =
    run(query[Todo].filter(_.id == lift(id)))
      .map(_.headOption)
      .some
      .mapError(_.getOrElse(NotFound))

  override def add(entity: Todo): Task[Todo] =
    run(quote(
      query[Todo]
        .insertValue(lift(entity))
        .returning(r => r)
    ))

  override def findAllByUserId(userId: Int): Task[List[Todo]] =
    run(
      query[Todo]
        .filter(_.parentId == lift(userId))
    )

  override def markCompleted(id: Int): Task[Todo] =
    run(quote(
      query[Todo]
        .filter(_.id == lift(id))
        .update(_.completed -> true)
        .returning(r => r)
    ))
}

object TodoRepo {
  val live: URLayer[Quill[PostgresDialect, SnakeCase], TodoRepo] =
    ZLayer.fromFunction(TodoRepoLive.apply)

  def get(id: Int): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.get(id))

  def add(todo: Todo): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.add(todo))

  def markCompleted(id: Int): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.markCompleted(id))

  def findAllByUserId(userId: Int): RIO[TodoRepo, List[Todo]] =
    ZIO.serviceWithZIO[TodoRepo](_.findAllByUserId(userId))

  def ownedBy(id: Int, userId: Int): RIO[TodoRepo, Boolean] =
    ZIO.serviceWithZIO[TodoRepo](_.ownedBy(id, userId))
}
