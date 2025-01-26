package api.server

import api.JwtContent
import api.auth.*
import api.endpoints.*
import api.services.{ AuthService, JwtService, TodoService, UserService }
import zio.*
import zio.http.Server
import zio.http.codec.HttpCodec.*

case class TodoServer(
    userEndpoints: UserEndpoints,
    todoEndpoints: TodoEndpoints,
    authEndpoints: AuthEndpoints,
) {

  def start: URIO[JwtService & Auth[JwtContent] & Server, Nothing] = {
    val endpoints = userEndpoints.all ++ authEndpoints.all ++ todoEndpoints.all
    Server.serve(
      httpApp = endpoints
    )
  }

}

object TodoServer {
  private type TodoServerEnv = UserService & TodoService & AuthService & JwtService

  val live: ZLayer[TodoServerEnv, Nothing, TodoServer] =
    ZLayer.makeSome[TodoServerEnv, TodoServer](
      ZLayer.fromFunction(TodoServer.apply),
      UserEndpoints.make,
      TodoEndpoints.make,
      AuthEndpoints.make,
    )

}
