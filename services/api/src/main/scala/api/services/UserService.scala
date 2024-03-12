package api.services

import api.auth.PasswordEncoder
import api.errors.ApiError
import api.request.UserRegister
import api.response.UserResponse
import db.repos.UserRepo
import io.github.arainko.ducktape.*
import users.user.ZioUser.UserServiceClient
import users.user.{Username, User as GUser}
import zio.*
import zio.http.*
import zio.http.endpoint.*
import zio.json.*
import zio.schema.Schema

import java.util.UUID

trait UserService {
  def add(registerDTO: UserRegister): IO[ApiError, UserResponse]
}

case class UserServiceLive(
    userRepo: UserRepo,
    passwordEncoder: PasswordEncoder,
) extends UserService {

  override def add(registerDTO: UserRegister): IO[ApiError, UserResponse] =
    for {
      _    <- exists(registerDTO.username)
        .filterOrFail(!_)(ApiError.UsernameTaken)
      user <- userRepo
        .add(
          registerDTO.copy(
            password = passwordEncoder.encode(registerDTO.password)
          )
        ).orDie
    } yield user.to[UserResponse]

  private def exists(username: String) =
    userRepo
      .findByUsername(username)
      .either
      .map(_.isRight)

}

case class UserServiceV2(userServiceClient: UserServiceClient, passwordEncoder: PasswordEncoder) extends UserService {

  override def add(registerDTO: UserRegister): IO[ApiError, UserResponse] =
    for {
      _    <- exists(registerDTO.username)
        .filterOrFail(!_)(ApiError.UsernameTaken)
      user <- userServiceClient
        .addUser(GUser(registerDTO.username, passwordEncoder.encode(registerDTO.password)))
        .orElseFail(ApiError.InternalError)
    } yield user.to[UserResponse]

  private def exists(username: String) =
    userServiceClient
      .getUserByUsername(Username(username))
      .either
      .map(_.isRight)

}

object UserService {

  val live: URLayer[UserRepo & PasswordEncoder, UserService] =
    ZLayer.fromFunction(UserServiceLive.apply)

  val v2_grpc: URLayer[UserServiceClient & PasswordEncoder, UserService] =
    ZLayer.fromFunction(UserServiceV2.apply)

}
