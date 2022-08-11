package repos.user

import domain.User
import domain.dto.UserRegister
import repos.InMemoryRepo
import zio.*

import java.util.UUID

case class UserRepoInMemory(inMemoryRepo: InMemoryRepo[User, UUID]) extends UserRepo {
  override def add(entity: User): Task[User] =
    inMemoryRepo.add(entity)

  override def get(id: UUID): Task[User] =
    inMemoryRepo.get(id)

  override def findByUsername(username: String): Task[User] =
    inMemoryRepo.find(_.username == username)
}