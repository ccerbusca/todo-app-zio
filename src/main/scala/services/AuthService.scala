package services
import domain.User
import domain.dto.UserAuthenticate
import domain.errors.CustomError.*
import repos.user.UserRepo
import zio.*

trait AuthService {
  def authenticate(user: UserAuthenticate): ZIO[Any, Throwable, User]
}

case class AuthServiceLive(userRepo: UserRepo) extends AuthService {
  override def authenticate(authDTO: UserAuthenticate): ZIO[Any, Throwable, User] =
    userRepo
      .findByUsername(authDTO.username)
      .filterOrFail(_.password == authDTO.password)(WrongCredentials)
}

object AuthService {
  val live: ZLayer[UserRepo, Nothing, AuthServiceLive] =
    ZLayer.fromFunction(AuthServiceLive.apply)

  def authenticate(authDTO: UserAuthenticate): ZIO[AuthService, Any, User] =
    ZIO.serviceWithZIO(_.authenticate(authDTO))
}
