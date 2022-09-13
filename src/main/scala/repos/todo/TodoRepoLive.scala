package repos.todo

import domain.Todo
import domain.errors.ApiError.*
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

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
