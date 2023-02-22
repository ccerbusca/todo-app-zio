package auth

import domain.User
import domain.api.request.UserAuthenticate
import domain.errors.ApiError
import domain.errors.ApiError.*
import services.AuthService
import zio.*
import zio.http.*

trait Auth[A] {
  def authContext: IO[ApiError, A]
  def setContext(e: Option[A]): UIO[Unit]
}

object Auth {

  def authContext[A: Tag]: ZIO[Auth[A], ApiError, A] =
    ZIO.serviceWithZIO[Auth[A]](_.authContext)

  def setContext[A: Tag](e: Option[A]): URIO[Auth[A], Unit] =
    ZIO.serviceWithZIO[Auth[A]](_.setContext(e))

  def authLayer[A: Tag]: ULayer[Auth[A]] = ZLayer.scoped {
    FiberRef
      .make[Option[A]](None)
      .map { ref =>
        new Auth[A] {
          override def authContext: IO[ApiError, A]        = ref.get.flatMap {
            case Some(value) => ZIO.succeed(value)
            case None        => ZIO.fail(Unauthorized)
          }
          override def setContext(e: Option[A]): UIO[Unit] = ref.set(e)
        }
      }
  }

}

val authMiddleware: RequestHandlerMiddleware[Auth[User] & AuthService, Response] =
  Middleware.basicAuthZIO { credentials =>
    for {
      user <- AuthService
        .authenticate(UserAuthenticate.fromCredentials(credentials))
        .mapError(apiError => Response.status(apiError.status))
      _    <- Auth.setContext(Some(user))
    } yield true
  }
