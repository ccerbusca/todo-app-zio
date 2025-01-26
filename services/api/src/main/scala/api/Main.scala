package api

import api.auth.{ Auth, PasswordEncoder }
import api.server.TodoServer
import api.services.{ AuthService, JwtService, TodoService, UserService }
import db.repos.{ TodoRepo, UserRepo }
import zio.http.Server
import zio.{ ZIO, ZIOAppDefault }

object Main extends ZIOAppDefault {

  override def run: ZIO[Any, Throwable, Nothing] =
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
        JwtService.live,
        PasswordEncoder.live,
        db.postgresDefault,
        Auth[JwtContent],
      )

}
