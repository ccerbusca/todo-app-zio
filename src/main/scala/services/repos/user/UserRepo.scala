package services.repos.user

import domain.User
import domain.dto.UserRegistrationDTO
import domain.errors.CustomError.*
import services.repos.*
import zhttp.http.*
import zio.*
import zio.json.*

import java.util.UUID

trait UserRepo extends Repo[User, UUID] {
  def get(username: String): Task[User]
  def add(registerDTO: UserRegistrationDTO): Task[User]
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

  def get(id: UUID): ZIO[UserRepo, Throwable, User] =
    ZIO.serviceWithZIO(_.get(id))

  def add(entity: UserRegistrationDTO): ZIO[UserRepo, Throwable, User] =
    ZIO.serviceWithZIO(_.add(entity))

  val endpoints: HttpApp[UserRepo, Throwable] = Http.collectZIO[Request] {
    case req @ Method.POST -> !! / "register" =>
      for {
        body <- req.bodyAsString
        registerDTO <- ZIO.fromEither(body.fromJson[UserRegistrationDTO])
          .mapError(new RuntimeException(_))
        userWithId <- UserRepo.add(registerDTO)
      } yield Response.json(userWithId.toJson)
  }
}

