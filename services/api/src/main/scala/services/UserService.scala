package services

import api.request.UserRegister
import api.response.UserResponse
import auth.PasswordEncoder
import domain.errors.ApiError
import io.github.arainko.ducktape.*
import repos.UserRepo
import zio.*
import zio.http.*
import zio.http.endpoint.*
import zio.http.model.*
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

object UserService {

  val live: URLayer[UserRepo & PasswordEncoder, UserService] =
    ZLayer.fromFunction(UserServiceLive.apply)

}
