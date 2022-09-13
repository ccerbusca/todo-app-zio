package services
import auth.PasswordEncoder
import domain.User
import domain.dto.request.UserAuthenticate
import domain.errors.ApiError.*
import repos.user.UserRepo
import zio.*

trait AuthService {
  def authenticate(user: UserAuthenticate): ZIO[Any, Throwable, User]
}

case class AuthServiceLive(userRepo: UserRepo, passwordEncoder: PasswordEncoder) extends AuthService {
  override def authenticate(authDTO: UserAuthenticate): ZIO[Any, Throwable, User] =
    userRepo
      .findByUsername(authDTO.username)
      .filterOrFail(user => passwordEncoder.verify(authDTO.password, user.password))(WrongAuthInfo)
}

object AuthService {
  val live: ZLayer[UserRepo & PasswordEncoder, Nothing, AuthServiceLive] =
    ZLayer.fromFunction(AuthServiceLive.apply)

  def authenticate(authDTO: UserAuthenticate): ZIO[AuthService, Throwable, User] =
    ZIO.serviceWithZIO[AuthService](_.authenticate(authDTO))
}
