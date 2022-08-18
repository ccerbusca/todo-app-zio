package repos.user

import io.getquill.*
import io.getquill.jdbczio.Quill
import domain.User
import domain.dto.UserRegister
import domain.errors.CustomError.*
import zio.*

import java.sql.SQLException
import java.util.UUID

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
