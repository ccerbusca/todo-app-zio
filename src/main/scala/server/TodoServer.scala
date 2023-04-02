package server

import auth.*
import domain.*
import domain.api.JwtContent
import domain.api.request.UserRegister
import domain.errors.ApiError
import endpoints.*
import services.{ AuthService, JwtService, TodoService, UserService }
import zio.*
import zio.http.codec.HttpCodec.*
import zio.http.endpoint.Endpoint
import zio.http.model.{ Method, Status }
import zio.http.{ Middleware, Response, Server }

case class TodoServer(
    userEndpoints: UserEndpoints,
    todoEndpoints: TodoEndpoints,
    authEndpoints: AuthEndpoints,
) {

  def start =
    Server.serve(
      httpApp = (userEndpoints.all ++ todoEndpoints.all ++ authEndpoints.all) @@ Middleware.debug
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
