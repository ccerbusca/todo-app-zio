import auth.{AuthContext, AuthMiddleware}
import domain.{Todo, User}
import domain.dto.UserAuthenticate
import io.getquill.SnakeCase
import repos.InMemoryRepo
import repos.todo.TodoRepo
import repos.user.UserRepo
import io.getquill.jdbczio.Quill
import services.{AuthService, TodoService, UserService}
import zhttp.http.*
import zhttp.http.middleware.HttpMiddleware
import zhttp.service.*
import zio.*
import zio.concurrent.ConcurrentMap
import domain.errors.CustomError.MissingCredentials
import services.generators.IdGenerator

import java.io.IOException
import java.util.UUID

object Main extends ZIOAppDefault {
  val authMiddleware: HttpMiddleware[AuthService, Nothing] =
    Middleware.basicAuthZIO { credentials =>
      AuthService.authenticate(UserAuthenticate.fromCredentials(credentials)).isSuccess
    }
    
  val customBasicAuth: Middleware[AuthService, Throwable, AuthContext[User], Response, Request, Response] =
    AuthMiddleware.customBasicAuth(MissingCredentials) { credentials =>
      AuthService.authenticate(UserAuthenticate.fromCredentials(credentials))
    }

  val unsecureEndpoints: HttpApp[UserService, Throwable] =
    UserService.endpoints ++
      Http.collect[Request] {
        case Method.GET -> !! / "test" => Response.text("123")
      }

  val securedEndpoints: HttpApp[AuthService & TodoService, Throwable] =
    TodoService.endpoints @@ customBasicAuth

  override def run: Task[Nothing] =
    Server
      .start(port = 8080, http = (unsecureEndpoints ++ securedEndpoints) @@ Middleware.debug)
      .provide(
        UserRepo.live,
//        UserRepo.inMemory,
        TodoRepo.inMemory,
        AuthService.live,
        UserService.live,
        TodoService.live,
//        InMemoryRepo.live[User, UUID],
        InMemoryRepo.live[Todo, Int],
//        ZLayer.fromZIO(ConcurrentMap.empty[UUID, User]),
        ZLayer.fromZIO(ConcurrentMap.empty[Int, Todo]),
        Quill.Postgres.fromNamingStrategy(SnakeCase),
        Quill.DataSource.fromPrefix("myDatabaseConfig"),
        IdGenerator.int
      )
}