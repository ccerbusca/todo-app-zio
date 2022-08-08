import domain.{Todo, User}
import domain.dto.AuthenticateDTO
import services.Authentication
import services.repos.InMemoryRepo
import services.repos.todo.TodoRepo
import services.repos.user.UserRepo
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

  val endpointsToSecure: HttpApp[TodoRepo, Throwable] = TodoRepo.endpoints

  val unsecureEndpoints: HttpApp[UserRepo, Throwable] = Http.collect[Request] {
    case Method.GET -> !! / "test" => Response.text("123")
  } ++ UserRepo.endpoints

  val secured: HttpApp[Authentication & UserRepo & TodoRepo, Throwable] = endpointsToSecure @@ authMiddleware

  override def run: Task[Nothing] =
    Server
      .start(port = 8080, http = (unsecureEndpoints ++ secured) @@ Middleware.debug)
      .provide(
        UserRepo.inMemory,
        TodoRepo.inMemory,
        Authentication.live,
        InMemoryRepo.live[User, UUID],
        InMemoryRepo.live[Todo, UUID],
        ZLayer.fromZIO(ConcurrentMap.empty[UUID, User]),
        ZLayer.fromZIO(ConcurrentMap.empty[UUID, Todo])
      )
}