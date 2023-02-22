package server

import auth.*
import domain.*
import domain.api.request.UserRegister
import domain.errors.ApiError
import endpoints.*
import services.{AuthService, TodoService, UserService}
import zio.*
import zio.http.codec.HttpCodec.*
import zio.http.endpoint.Endpoint
import zio.http.model.{Method, Status}
import zio.http.{Middleware, Response, Server}

case class TodoServer() {

  def start =
    for {
      userEndpoints <- UserEndpoints.make
      todoEndpoints <- TodoEndpoints.make
      securedEndpoints = (todoEndpoints @@ authMiddleware).provideSomeLayer(Auth.authLayer[User])
      _ <- Server.serve(
        httpApp = (userEndpoints ++ securedEndpoints) @@ Middleware.debug
      )
    } yield ()

}

object TodoServer {
  val live = ZLayer.succeed(TodoServer())
}
