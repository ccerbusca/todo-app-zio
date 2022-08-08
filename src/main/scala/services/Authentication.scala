package services
import domain.User
import domain.dto.AuthenticateDTO
import domain.errors.CustomError.*
import services.repos.user.UserRepo
import zio.*

trait Authentication {
  def authenticate(user: AuthenticateDTO): ZIO[Any, Throwable, User]
}

case class AuthenticationLive(userRepo: UserRepo) extends Authentication {
  override def authenticate(authDTO: AuthenticateDTO): ZIO[Any, Throwable, User] =
    userRepo
      .get(authDTO.username)
      .filterOrFail(_.password == authDTO.password)(WrongCredentials)
}

object Authentication {
  val live: ZLayer[UserRepo, Nothing, AuthenticationLive] = ZLayer {
    ZIO.service[UserRepo].map(AuthenticationLive.apply)
  }

  def authenticate(authDTO: AuthenticateDTO): ZIO[Authentication, Any, User] =
    ZIO.serviceWithZIO(_.authenticate(authDTO))
}
