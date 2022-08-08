package services.repos

import domain.User
import domain.dto.UserRegistrationDTO
import domain.errors.CustomError.*
import zhttp.http.*
import zio.*
import zio.json.*

import java.util.UUID

trait UserRepo extends Repo[User, UUID] {
  def get(username: String): ZIO[Any, Throwable, User]
  def add(registerDTO: UserRegistrationDTO): ZIO[Any, Throwable, User]
}

case class UserRepoLive(dbRef: Ref[Map[UUID, User]]) extends UserRepo {
  override def get(uuid: UUID): ZIO[Any, Throwable, User] =
    for {
      innerDB <- dbRef.get
      user <- ZIO.fromOption(innerDB.get(uuid)).orElseFail(NotFound())
    } yield user

  override def add(registerDTO: UserRegistrationDTO): ZIO[Any, Nothing, User] =
    for {
      uuid <- Random.nextUUID
      user = User(registerDTO.username, registerDTO.password, uuid)
      _ <- dbRef.update(innerDB => innerDB + (uuid -> user))
    } yield user

  override def get(username: String): ZIO[Any, Throwable, User] =
    for {
      db <- dbRef.get
      userResult <- ZIO.fromOption(db.values.find(_.username == username)).orElseFail(NotFound())
    } yield userResult
}

case class UserRepoInMemory(inMemoryRepo: InMemoryRepo[User, UUID]) extends UserRepo {
  override def get(username: String): ZIO[Any, Throwable, User] =
    inMemoryRepo.find(_.username == username)

  override def add(registerDTO: UserRegistrationDTO): ZIO[Any, Throwable, User] =
    for {
      uuid <- Random.nextUUID
      newUser = User(registerDTO.username, registerDTO.password, uuid)
      user <- inMemoryRepo.add(newUser)
    } yield user

  override def get(uuid: UUID): ZIO[Any, Throwable, User] = inMemoryRepo.get(uuid)
}

object UserRepo {
  val live: URLayer[Ref[Map[UUID, User]], UserRepoLive] =
    ZLayer {
      ZIO.service[Ref[Map[UUID, User]]].map(UserRepoLive.apply)
    }
    
  val inMemory: ZLayer[InMemoryRepo[User, UUID], Nothing, UserRepoInMemory] =
    ZLayer {
      ZIO.service[InMemoryRepo[User, UUID]].map(UserRepoInMemory.apply)
    }

  def get(uuid: UUID): ZIO[UserRepo, Throwable, User] =
    ZIO.serviceWithZIO(_.get(uuid))

  def add(entity: UserRegistrationDTO): ZIO[UserRepo, Throwable, User] =
    ZIO.serviceWithZIO(_.add(entity))

  val endpoints = Http.collectZIO[Request] {
    case req @ Method.POST -> !! / "register" =>
      for {
        body <- req.bodyAsString
        registerDTO <- ZIO.fromEither(body.fromJson[UserRegistrationDTO])
          .mapError(new RuntimeException(_))
        userWithId <- UserRepo.add(registerDTO)
      } yield Response.json(userWithId.toJson)
  }
}

