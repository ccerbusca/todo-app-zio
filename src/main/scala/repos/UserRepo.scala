package repos

import domain.User
import domain.api.request.UserRegister
import domain.errors.ApiError.*
import io.getquill.*
import io.getquill.jdbczio.Quill
import repos.Repo
import zio.*

import java.util.UUID

trait UserRepo extends Repo[User, Int] {
  def findByUsername(username: String): Task[User]
}

case class UserRepoLive(quill: Quill[PostgresDialect, SnakeCase]) extends UserRepo {
  import quill.*
  given SchemaMeta[User] = schemaMeta[User]("users")

  override def get(id: Int): Task[User] =
    run(query[User].filter(_.id == lift(id)))
      .map(_.headOption)
      .some
      .mapError(_.getOrElse(NotFound))

  override def add(user: User): Task[User] =
    run(quote(
      query[User].insertValue(lift(user))
    ))
      .filterOrFail(_ > 0)(FailedInsert)
      .as(user)

  override def findByUsername(username: String): Task[User] =
    run(query[User].filter(_.username == lift(username)))
      .map(_.headOption)
      .some
      .mapError(_.getOrElse(NotFound))
}

object UserRepo {
  val live: URLayer[Quill[PostgresDialect, SnakeCase], UserRepo] =
    ZLayer.fromFunction(UserRepoLive.apply)

  def get(id: Int): ZIO[UserRepo, Throwable, User] =
    ZIO.serviceWithZIO(_.get(id))

  def add(entity: User): ZIO[UserRepo, Throwable, User] =
    ZIO.serviceWithZIO(_.add(entity))
    
  def findByUsername(username: String): RIO[UserRepo, User] =
    ZIO.serviceWithZIO[UserRepo](_.findByUsername(username))
}

