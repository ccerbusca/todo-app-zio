package repos

import api.request.AddTodo
import entities.*
import io.getquill.*
import todos.todo.Todo as GTodo
import zio.*

import java.util.UUID

trait TodoRepo {
  def findAllByUserId(userId: User.ID): Task[List[Todo]]
  def markCompleted(id: Todo.ID): Task[Todo]

  def ownedBy(id: Todo.ID, userId: User.ID): Task[Boolean] =
    get(id).map(_.exists(_.parentId == userId))

  def get(id: Todo.ID): Task[Option[Todo]]
  def add(entity: GTodo, userId: User.ID): Task[Todo]

  def delete(id: Todo.ID): Task[Todo]
}

case class TodoRepoLive(quill: db.QuillPostgres) extends TodoRepo {
  import quill.*

  inline given InsertMeta[Todo] = insertMeta(_.id)

  override def get(id: Todo.ID): Task[Option[Todo]] =
    run(query[Todo].filter(_.id == lift(id)))
      .map(_.headOption)

  override def add(entity: GTodo, userId: User.ID): Task[Todo] =
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

  val live: URLayer[db.QuillPostgres, TodoRepo] =
    ZLayer.fromFunction(TodoRepoLive.apply)

  def get(id: Int): RIO[TodoRepo, Option[Todo]] =
    ZIO.serviceWithZIO[TodoRepo](_.get(id))

  def add(todo: GTodo, userId: User.ID): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.add(todo, userId))

  def markCompleted(id: Int): RIO[TodoRepo, Todo] =
    ZIO.serviceWithZIO[TodoRepo](_.markCompleted(id))

  def findAllByUserId(userId: Int): RIO[TodoRepo, List[Todo]] =
    ZIO.serviceWithZIO[TodoRepo](_.findAllByUserId(userId))

  def ownedBy(id: Int, userId: Int): RIO[TodoRepo, Boolean] =
    ZIO.serviceWithZIO[TodoRepo](_.ownedBy(id, userId))

}
