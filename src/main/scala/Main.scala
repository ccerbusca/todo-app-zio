import auth.{AuthContext, AuthMiddleware, PasswordEncoder}
import domain.dto.request.UserAuthenticate
import domain.errors.CustomError.MissingCredentials
import domain.{Todo, User}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import repos.InMemoryRepo
import repos.todo.TodoRepo
import repos.user.UserRepo
import services.generators.IdGenerator
import services.{AuthService, TodoService, UserService}
import zhttp.http.*
import zhttp.http.middleware.HttpMiddleware
import zhttp.service.*
import zio.*
import zio.concurrent.ConcurrentMap

import java.io.IOException
import java.util.UUID

object Main extends ZIOAppDefault {
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
    TodoService.secureEndpoints @@ customBasicAuth

  override def run: Task[Nothing] =
    Server
      .start(port = 8080, http = (unsecureEndpoints ++ securedEndpoints) @@ Middleware.debug)
      .provide(
        UserRepo.live,
        TodoRepo.live,
        AuthService.live,
        UserService.live,
        TodoService.live,
        Quill.Postgres.fromNamingStrategy(SnakeCase),
        Quill.DataSource.fromPrefix("postgresConfig"),
        IdGenerator.int,
        PasswordEncoder.live
      )
}