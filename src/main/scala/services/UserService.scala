package services

import auth.PasswordEncoder
import domain.User
import domain.api.request.UserRegister
import domain.api.response.UserResponse
import domain.errors.ApiError
import domain.generators.Generator
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
        .filterOrFail(identity)(ApiError.UsernameTaken)
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
      .map(_.left.exists(_ eq ApiError.NotFound))

}

object UserService {

  val live: URLayer[UserRepo & PasswordEncoder, UserService] =
    ZLayer.fromFunction(UserServiceLive.apply)

}
