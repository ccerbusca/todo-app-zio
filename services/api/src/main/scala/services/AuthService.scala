package services

import api.request.UserAuthenticate
import auth.PasswordEncoder
import domain.*
import domain.errors.ApiError
import io.github.arainko.ducktape.*
import repos.UserRepo
import users.user.Username
import users.user.ZioUser.UserServiceClient
import users.user.ZioUser.UserServiceClient.*
import zio.*
trait AuthService {
  def authenticate(user: UserAuthenticate): ZIO[Any, ApiError, User]
}

case class AuthServiceLive(userRepo: UserRepo, passwordEncoder: PasswordEncoder) extends AuthService {

  override def authenticate(authDTO: UserAuthenticate): ZIO[Any, ApiError, User] =
    userRepo
      .findByUsername(authDTO.username)
      .mapError(_ => ApiError.WrongAuthInfo)
      .filterOrFail(user => passwordEncoder.verify(authDTO.password, user.password))(ApiError.WrongAuthInfo)

}

case class AuthServiceV2(userServiceClient: UserServiceClient, passwordEncoder: PasswordEncoder) extends AuthService {

  override def authenticate(authDTO: UserAuthenticate): ZIO[Any, ApiError, User] =
    userServiceClient
      .getUserByUsername(Username(authDTO.username))
      .map(_.toDomain)
      .some
      .mapError(_ => ApiError.Unauthorized)
      .filterOrFail(user => passwordEncoder.verify(authDTO.password, user.password))(ApiError.WrongAuthInfo)

}

object AuthService {

  val live: ZLayer[UserRepo & PasswordEncoder, Nothing, AuthServiceLive] =
    ZLayer.fromFunction(AuthServiceLive.apply)
    
  val v2_grpc: ZLayer[UserServiceClient & PasswordEncoder, Nothing, AuthService] =
    ZLayer.fromFunction(AuthServiceV2.apply)

}
