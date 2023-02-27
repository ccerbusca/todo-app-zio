import auth.*
import domain.api.request.UserAuthenticate
import domain.errors.ApiError.MissingCredentials
import domain.generators.Generator
import domain.{ Todo, User }
import endpoints.*
import io.getquill.jdbczio.Quill
import io.getquill.{ PostgresZioJdbcContext, SnakeCase }
import repos.{ db, TodoRepo, UserRepo }
import server.TodoServer
import services.{ AuthService, TodoService, UserService }
import zio.*
import zio.concurrent.ConcurrentMap
import zio.http.*
import zio.http.middleware.RequestHandlerMiddlewares
import zio.http.model.*
import zio.http.service.*
import zio.stream.*

import java.io.IOException
import java.util.UUID

object Main extends ZIOAppDefault {

  override def run =
    ZIO
      .serviceWithZIO[TodoServer](_.start)
      .provide(
        TodoServer.live,
        Server.defaultWithPort(8080),
        UserRepo.live,
        TodoRepo.live,
        AuthService.live,
        UserService.live,
        TodoService.live,
        Generator.int(),
        PasswordEncoder.live,
        db.postgresDefault,
      )

}
