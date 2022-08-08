import domain.User
import domain.dto.AuthenticateDTO
import services.Authentication
import services.repos.UserRepo
import zhttp.service.*
import zio.*
import zhttp.http.*
import zhttp.http.middleware.HttpMiddleware

import java.io.IOException
import java.util.UUID

object Main extends ZIOAppDefault {
  val endpoints: HttpApp[UserRepo, Throwable] = UserRepo.endpoints

  val middleware: HttpMiddleware[UserRepo & Authentication, IOException] =
    Middleware.basicAuthZIO(c => Authentication.authenticate(AuthenticateDTO.fromCredentials(c)).isSuccess) ++ Middleware.debug

  val httpApp: HttpApp[UserRepo & Authentication, Throwable] = endpoints

  val endpoints2 = Http.collect[Request] {
    case Method.GET -> !! / "test" => Response.text("123")
  }

  val secured = endpoints2 @@ middleware

  override def run =
    Server
      .start(port = 8080, http = (httpApp @@ Middleware.debug) ++ secured)
      .provide(
        UserRepo.live,
        Authentication.live,
        ZLayer.fromZIO(Ref.make(Map.empty[UUID, User]))
      )
}