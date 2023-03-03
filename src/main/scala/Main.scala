import auth.PasswordEncoder
import domain.generators.Generator
import endpoints.*
import repos.*
import server.TodoServer
import services.*
import zio.*
import zio.http.*
import zio.http.service.*

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
        JwtService.live,
        PasswordEncoder.live,
        db.postgresDefault,
      )

}
