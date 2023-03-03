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
  def get(id: Int): IO[ApiError, UserResponse]
  def add(registerDTO: UserRegister): IO[ApiError, UserResponse]
  def findByUsername(username: String): IO[ApiError, UserResponse]
}

case class UserServiceLive(
    userRepo: UserRepo,
    passwordEncoder: PasswordEncoder,
) extends UserService {

  override def get(id: Int): IO[ApiError, UserResponse] =
    userRepo
      .get(id)
      .map(_.to[UserResponse])

  override def add(registerDTO: UserRegister): IO[ApiError, UserResponse] =
    for {
      user <- userRepo.add(
        registerDTO.copy(
          password = passwordEncoder.encode(registerDTO.password)
        )
      )
    } yield user.to[UserResponse]

  override def findByUsername(username: String): IO[ApiError, UserResponse] =
    userRepo
      .findByUsername(username)
      .map(_.to[UserResponse])

}

object UserService {

  val live: URLayer[UserRepo & PasswordEncoder, UserService] =
    ZLayer.fromFunction(UserServiceLive.apply)

}
