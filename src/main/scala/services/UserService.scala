package services

import domain.User
import domain.dto.UserRegister
import repos.user.UserRepo
import zhttp.http.*
import zio.*
import zio.json.*

import java.util.UUID

trait UserService {
  def get(id: UUID): Task[User]
  def add(registerDTO: UserRegister): Task[User]
  def findByUsername(username: String): Task[User]
}

case class UserServiceLive(userRepo: UserRepo) extends UserService {
  override def get(id: UUID): Task[User] =
    userRepo.get(id)

  override def add(registerDTO: UserRegister): Task[User] =
    for {
      uuid <- Random.nextUUID
      newUser = User(registerDTO.username, registerDTO.password, uuid)
      user <- userRepo.add(newUser)
    } yield user

  override def findByUsername(username: String): Task[User] =
    userRepo.findByUsername(username)
}

object UserService {
  val live: URLayer[UserRepo, UserService] =
    ZLayer.fromFunction(UserServiceLive.apply)

  def add(registerDTO: UserRegister): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.add(registerDTO))

  def get(id: UUID): ZIO[UserService, Throwable, User] =
    ZIO.serviceWithZIO[UserService](_.get(id))

  val endpoints: HttpApp[UserService, Throwable] = Http.collectZIO[Request] {
    case req@Method.POST -> !! / "register" =>
      for {
        body <- req.bodyAsString
        registerDTO <- ZIO.fromEither(body.fromJson[UserRegister])
          .mapError(new RuntimeException(_))
        userWithId <- UserService.add(registerDTO)
      } yield Response.json(userWithId.toJson)
  }
}
