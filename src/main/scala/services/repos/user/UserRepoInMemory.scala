package services.repos.user

import domain.User
import domain.dto.UserRegistrationDTO
import services.repos.InMemoryRepo
import zio.*

import java.util.UUID

case class UserRepoInMemory(inMemoryRepo: InMemoryRepo[User, UUID]) extends UserRepo {
  override def get(username: String): Task[User] =
    find(_.username == username)

  override def add(registerDTO: UserRegistrationDTO): Task[User] =
    for {
      uuid <- Random.nextUUID
      newUser = User(registerDTO.username, registerDTO.password, uuid)
      user <- inMemoryRepo.add(newUser)
    } yield user

  override def get(id: UUID): Task[User] =
    inMemoryRepo.get(id)

  override def find(pred: User => Boolean): Task[User] =
    inMemoryRepo.find(pred)
}
