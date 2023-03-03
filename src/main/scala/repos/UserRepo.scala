package repos

import domain.User
import domain.api.request.UserRegister
import domain.errors.ApiError
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import java.util.UUID

trait UserRepo {
  def findByUsername(username: String): IO[ApiError, User]

  def get(id: User.ID): IO[ApiError, User]

  def add(entity: UserRegister): IO[ApiError, User]
}

case class UserRepoLive(quill: Quill[PostgresDialect, SnakeCase]) extends UserRepo {
  import quill.*
  inline given SchemaMeta[User] = schemaMeta[User]("users")

  override def get(id: Int): IO[ApiError, User] =
    run(query[User].filter(_.id == lift(id)))
      .map(_.headOption)
      .some
      .orElseFail(ApiError.NotFound)

  override def add(user: UserRegister): IO[ApiError, User] =
    run(
      quote(
        query[User]
          .insertValue(lift(User(0, user.username, user.password)))
          .returning(r => r)
      )
    )
      .orElseFail(ApiError.FailedInsert)

  override def findByUsername(username: String): IO[ApiError, User] =
    run(query[User].filter(_.username == lift(username)))
      .map(_.headOption)
      .some
      .orElseFail(ApiError.NotFound)

}

object UserRepo {

  val live: URLayer[Quill[PostgresDialect, SnakeCase], UserRepo] =
    ZLayer.fromFunction(UserRepoLive.apply)

  def get(id: Int): ZIO[UserRepo, ApiError, User] =
    ZIO.serviceWithZIO(_.get(id))

  def add(entity: UserRegister): ZIO[UserRepo, ApiError, User] =
    ZIO.serviceWithZIO(_.add(entity))

  def findByUsername(username: String): RIO[UserRepo, User] =
    ZIO.serviceWithZIO[UserRepo](_.findByUsername(username))

}
