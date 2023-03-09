package repos

import api.request.UserRegister
import entities.*
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import java.util.UUID

trait UserRepo {
  def findByUsername(username: String): Task[Option[User]]

  def get(id: User.ID): Task[Option[User]]

  def add(entity: UserRegister): Task[User]
}

case class UserRepoLive(quill: Quill[PostgresDialect, SnakeCase]) extends UserRepo {
  import quill.*
  inline given SchemaMeta[User] = schemaMeta("users")
  inline given InsertMeta[User] = insertMeta(_.id)

  override def get(id: User.ID): Task[Option[User]] =
    run(query[User].filter(_.id == lift(id)))
      .map(_.headOption)

  override def add(userRegister: UserRegister): Task[User] =
    run(
      quote(
        query[User]
          .insertValue(lift(User(0, userRegister.username, userRegister.password)))
          .returning(r => r)
      )
    )

  override def findByUsername(username: String): Task[Option[User]] =
    run(query[User].filter(_.username == lift(username)))
      .map(_.headOption)

}

object UserRepo {

  val live: URLayer[Quill[PostgresDialect, SnakeCase], UserRepo] =
    ZLayer.fromFunction(UserRepoLive.apply)

  def get(id: Int): RIO[UserRepo, Option[User]] =
    ZIO.serviceWithZIO(_.get(id))

  def add(entity: UserRegister): RIO[UserRepo, User] =
    ZIO.serviceWithZIO(_.add(entity))

  def findByUsername(username: String): RIO[UserRepo, Option[User]] =
    ZIO.serviceWithZIO[UserRepo](_.findByUsername(username))

}
