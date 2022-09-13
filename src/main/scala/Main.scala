import auth.{AuthContext, AuthMiddleware, PasswordEncoder}
import domain.dto.request.UserAuthenticate
import domain.errors.ApiError.MissingCredentials
import domain.{Todo, User}
import io.getquill.jdbczio.Quill
import io.getquill.{PostgresZioJdbcContext, SnakeCase}
import repos.InMemoryRepo
import repos.todo.TodoRepo
import repos.user.UserRepo
import services.generators.Generator
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

  val publicEndpoints: HttpApp[UserService, Throwable] =
    UserService.endpoints ++
      Http.collect[Request] {
        case Method.GET -> !! / "test" => Response.text("123")
      }

  val securedEndpoints: HttpApp[AuthService & TodoService, Throwable] =
    TodoService.secureEndpoints @@ customBasicAuth

  override def run: Task[Nothing] =
    Server
      .start(
        port = 8080,
        http = (
          publicEndpoints ++ securedEndpoints ++ Http.methodNotAllowed("Method not defined")
        ) @@ Middleware.debug
      )
      .provide(
        UserRepo.live,
        TodoRepo.live,
        AuthService.live,
        UserService.live,
        TodoService.live,
        Generator.int(),
        PasswordEncoder.live,
        Quill.DataSource.fromPrefix("postgresConfig"),
        Quill.Postgres.fromNamingStrategy(SnakeCase),
      )
}