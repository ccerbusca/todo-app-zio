package repos.todo

import domain.Todo
import domain.dto.request.AddTodo
import io.getquill.jdbczio.Quill
import io.getquill.{PostgresDialect, SnakeCase}
import repos.{InMemoryRepo, Repo}
import zio.*

import java.util.UUID

trait TodoRepo extends Repo[Todo, Int] {
  def findAllByUserId(userId: Int): Task[List[Todo]]
  def markCompleted(id: Int): Task[Todo]
  def ownedBy(id: Int, userId: Int): Task[Boolean] =
    get(id).map(_.parentId == userId)
}

object TodoRepo {
  val live: URLayer[Quill[PostgresDialect, SnakeCase], TodoRepo] =
    ZLayer.fromFunction(TodoRepoLive.apply)  
  
  val inMemory: URLayer[InMemoryRepo[Todo, Int], TodoRepo] =
    ZLayer.fromFunction(TodoRepoInMemory.apply)

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
