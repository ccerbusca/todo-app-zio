package endpoints

import api.request.UserRegister
import api.response.UserResponse
import domain.errors.ApiError
import services.UserService
import zio.http.{App, Status}
import zio.http.codec.HttpCodec.*
import zio.http.endpoint.*
import zio.http.endpoint.EndpointMiddleware.None
import zio.{ZIO, ZLayer}

case class UserEndpoints(userService: UserService) {

  val register: Routes[Any, ApiError, None] =
    UserEndpoints
      .register
      .implement(userService.add)

  val all = register.toApp
}

object UserEndpoints {

  val make = ZLayer.fromFunction(UserEndpoints.apply)

  private val register =
    Endpoint
      .post("register")
      .in[UserRegister]
      .out[UserResponse]
      .outError[ApiError](Status.BadRequest)

}
