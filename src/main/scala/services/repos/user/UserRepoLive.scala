package services.repos.user

import domain.User
import domain.dto.UserRegistrationDTO
import domain.errors.CustomError.NotFound
import zio.*

import java.util.UUID

case class UserRepoLive(dbRef: Ref[Map[UUID, User]]) extends UserRepo {
  override def get(id: UUID): Task[User] =
    for {
      innerDB <- dbRef.get
      user <- ZIO.fromOption(innerDB.get(id)).orElseFail(NotFound)
    } yield user

  override def add(registerDTO: UserRegistrationDTO): Task[User] =
    for {
      uuid <- Random.nextUUID
      user = User(registerDTO.username, registerDTO.password, uuid)
      _ <- dbRef.update(innerDB => innerDB + (uuid -> user))
    } yield user

  override def get(username: String): Task[User] =
    find(_.username == username)

  override def find(pred: User => Boolean): Task[User] =
    for {
      db <- dbRef.get
      res <- ZIO.fromOption(db.values.find(pred)).orElseFail(NotFound)
    } yield res
}
