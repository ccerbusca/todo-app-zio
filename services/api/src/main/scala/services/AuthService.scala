package services

import api.request.UserAuthenticate
import auth.PasswordEncoder
import domain.User
import domain.errors.ApiError
import repos.UserRepo
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

object AuthService {

  val live: ZLayer[UserRepo & PasswordEncoder, Nothing, AuthServiceLive] =
    ZLayer.fromFunction(AuthServiceLive.apply)

}
