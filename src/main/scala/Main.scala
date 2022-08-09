import domain.{Todo, User}
import domain.dto.UserAuthenticate
import repos.InMemoryRepo
import repos.todo.TodoRepo
import repos.user.UserRepo
import services.{AuthService, TodoService, UserService}
import zhttp.http.*
import zhttp.http.middleware.HttpMiddleware
import zhttp.service.*
import zio.*
import zio.concurrent.ConcurrentMap

import java.io.IOException
import java.util.UUID

object Main extends ZIOAppDefault {
  val authMiddleware: HttpMiddleware[AuthService, Nothing] =
    Middleware.basicAuthZIO {
      credentials => AuthService.authenticate(UserAuthenticate.fromCredentials(credentials)).isSuccess
    }

  val unsecureEndpoints: HttpApp[UserService, Throwable] =
    UserService.endpoints ++
      Http.collect[Request] {
        case Method.GET -> !! / "test" => Response.text("123")
      }

  val securedEndpoints: HttpApp[AuthService & TodoService, Throwable] =
    TodoService.endpoints @@ authMiddleware

  override def run: Task[Nothing] =
    Server
      .start(port = 8080, http = (unsecureEndpoints ++ securedEndpoints) @@ Middleware.debug)
      .provide(
        UserRepo.inMemory,
        TodoRepo.inMemory,
        AuthService.live,
        UserService.live,
        TodoService.live,
        InMemoryRepo.live[User, UUID],
        InMemoryRepo.live[Todo, UUID],
        ZLayer.fromZIO(ConcurrentMap.empty[UUID, User]),
        ZLayer.fromZIO(ConcurrentMap.empty[UUID, Todo])
      )
}