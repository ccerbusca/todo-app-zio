package server

import endpoints.*
import services.{AuthService, JwtService, TodoService, UserService}
import zio.*
import zio.http.Server
import zio.http.codec.HttpCodec.*

case class TodoServer(
    userEndpoints: UserEndpoints,
    todoEndpoints: TodoEndpoints,
    authEndpoints: AuthEndpoints,
) {

  def start: URIO[JwtService & Server, Nothing] =
    Server.serve(
      httpApp = userEndpoints.all ++ authEndpoints.all ++ todoEndpoints.all
    )

}

object TodoServer {
  private type TodoServerEnv = UserService & TodoService & AuthService & JwtService

  val live = ZLayer.makeSome[TodoServerEnv, TodoServer](
    ZLayer.fromFunction(TodoServer.apply),
    UserEndpoints.make,
    TodoEndpoints.make,
    AuthEndpoints.make,
  )

}
