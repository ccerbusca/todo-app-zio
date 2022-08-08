import domain.User
import domain.dto.AuthenticateDTO
import services.Authentication
import services.repos.{InMemoryRepo, UserRepo}
import zhttp.http.*
import zhttp.http.middleware.HttpMiddleware
import zhttp.service.*
import zio.*
import zio.concurrent.ConcurrentMap

import java.io.IOException
import java.util.UUID

object Main extends ZIOAppDefault {
  val authMiddleware: HttpMiddleware[Authentication, Nothing] =
    Middleware.basicAuthZIO {
      credentials => Authentication.authenticate(AuthenticateDTO.fromCredentials(credentials)).isSuccess
    }

  val endpointsToSecure: HttpApp[UserRepo, Throwable] = UserRepo.endpoints

  val unsecureEndpoints = Http.collect[Request] {
    case Method.GET -> !! / "test" => Response.text("123")
  }

  val secured = endpointsToSecure @@ authMiddleware

  override def run =
    Server
      .start(port = 8080, http = secured ++ unsecureEndpoints @@ Middleware.debug)
      .provide(
        UserRepo.inMemory,
        Authentication.live,
        InMemoryRepo.live[User, UUID],
        ZLayer.fromZIO(ConcurrentMap.empty[UUID, User])
      )
}