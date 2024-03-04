package auth

import api.JwtContent
import api.request.UserAuthenticate
import domain.errors.ApiError
import services.{ AuthService, JwtService }
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

  def apply[A: Tag]: ULayer[Auth[A]] = ZLayer.scoped {
    FiberRef
      .make[Option[A]](None)
      .map { ref =>
        new Auth[A] {
          override def authContext: IO[ApiError, A] = ref.get.flatMap {
            case Some(value) => ZIO.succeed(value)
            case None => ZIO.fail(ApiError.Unauthorized)
          }

          override def setContext(e: Option[A]): UIO[Unit] = ref.set(e)
        }
      }
  }
}

def authMiddleware[R]: HandlerAspect[R & JwtService, JwtContent] =
  Middleware
    .customAuthProvidingZIO[R & JwtService, JwtContent](headers =>
      headers.header(Header.Authorization) match {
        case Some(Header.Authorization.Bearer(token)) =>
          JwtService
            .decode(token)
            .either
            .map(_.toOption)
        case _                                        =>
          ZIO.none
      }
    )
