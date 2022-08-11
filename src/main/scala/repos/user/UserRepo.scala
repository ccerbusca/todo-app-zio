package repos.user

import domain.User
import domain.dto.UserRegister
import domain.errors.CustomError.*
import io.getquill.jdbczio.Quill
import io.getquill.{PostgresDialect, SnakeCase}
import repos.{InMemoryRepo, Repo}
import zio.*

import java.util.UUID

trait UserRepo extends Repo[User, UUID] {
  def findByUsername(username: String): Task[User]
}

object UserRepo {
  val live: URLayer[Quill[PostgresDialect, SnakeCase], UserRepo] =
    ZLayer.fromFunction(UserRepoLive.apply)

  val inMemory: URLayer[InMemoryRepo[User, UUID], UserRepo] =
    ZLayer.fromFunction(UserRepoInMemory.apply)

  def get(id: UUID): ZIO[UserRepo, Throwable, User] =
    ZIO.serviceWithZIO(_.get(id))

  def add(entity: User): ZIO[UserRepo, Throwable, User] =
    ZIO.serviceWithZIO(_.add(entity))
}
