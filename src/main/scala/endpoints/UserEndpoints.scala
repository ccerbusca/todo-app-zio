package endpoints

import domain.api.request.UserRegister
import domain.api.response.UserResponse
import domain.errors.ApiError
import services.UserService
import zio.http.App
import zio.http.codec.HttpCodec.*
import zio.http.endpoint.*
import zio.http.endpoint.EndpointMiddleware.None
import zio.http.model.Status
import zio.{ZIO, ZLayer, http}

case class UserEndpoints(userService: UserService) {

  val register: Routes[Any, ApiError, None] =
    UserEndpoints
      .register
      .implement(userService.add)

  val getUser: Routes[Any, ApiError, None] =
    UserEndpoints
      .getUser
      .implement(userService.get)

  val all = register ++ getUser
}

object UserEndpoints {

  val make: ZIO[UserService, Nothing, App[Any]] =
    for {
      userService <- ZIO.service[UserService]
      userEndpoints = UserEndpoints(userService)
      routes        = userEndpoints.all
    } yield routes.toApp

  private val register =
    Endpoint
      .post("register")
      .in[UserRegister]
      .out[UserResponse]
      .err[ApiError](Status.BadRequest)

  private val getUser =
    Endpoint
      .post("user" / int("userId"))
      .out[UserResponse]
      .err[ApiError](Status.BadRequest)

}
