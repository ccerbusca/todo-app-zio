package repos.todo

import domain.Todo
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*
import domain.errors.CustomError.*

case class TodoRepoLive(quill: Quill[PostgresDialect, SnakeCase]) extends TodoRepo {
  import quill.*

  override def get(id: Int): Task[Todo] =
    run(query[Todo].filter(_.id == lift(id)))
      .map(_.headOption)
      .some
      .mapError(_.getOrElse(NotFound))
      

  override def add(entity: Todo): Task[Todo] =
    run(quote(
      query[Todo].insertValue(lift(entity))
    ))
      .filterOrFail(_ > 0)(FailedInsert)
      .as(entity)

  override def findAllByParentId(parentId: Int): Task[List[Todo]] =
    run(query[Todo].filter(_.parentId == lift(parentId)))
}
