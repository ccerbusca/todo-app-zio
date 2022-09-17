package services

import auth.PasswordEncoder
import domain.User
import domain.dto.request.UserRegister
import repos.user.UserRepo
import services.generators.Generator
import zhttp.http.*
import zio.*
import zio.json.*

import java.util.UUID

trait UserService {
  def get(id: Int): Task[User]
  def add(registerDTO: UserRegister): Task[User]
  def findByUsername(username: String): Task[User]
}

case class UserServiceLive(
  userRepo: UserRepo,
  idGenerator: Generator[Int],
  passwordEncoder: PasswordEncoder
) extends UserService {
  override def get(id: Int): Task[User] =
    userRepo.get(id)

  override def add(registerDTO: UserRegister): Task[User] =
    for {
      id <- idGenerator.generate
      newUser = User(registerDTO.username, passwordEncoder.encode(registerDTO.password), id)
      user <- userRepo.add(newUser)
    } yield user

  override def findByUsername(username: String): Task[User] =
    userRepo.findByUsername(username)
}

object UserService {
  val live: URLayer[UserRepo & Generator[Int] & PasswordEncoder, UserService] =
    ZLayer.fromFunction(UserServiceLive.apply)

  def add(registerDTO: UserRegister): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.add(registerDTO))

  def get(id: Int): ZIO[UserService, Throwable, User] =
    ZIO.serviceWithZIO[UserService](_.get(id))

  val endpoints: HttpApp[UserService, Throwable] = Http.collectZIO[Request] {
    case req@Method.POST -> !! / "register" =>
      for {
        body <- req.body.asString
        registerDTO <- ZIO.fromEither(body.fromJson[UserRegister])
          .mapError(new RuntimeException(_))
        userWithId <- UserService.add(registerDTO)
      } yield Response.json(userWithId.toJson)
  }
}
