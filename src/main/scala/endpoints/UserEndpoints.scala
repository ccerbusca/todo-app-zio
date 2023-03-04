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
import zio.{ZIO, ZLayer}

case class UserEndpoints(userService: UserService) {

  val register: Routes[Any, ApiError, None] =
    UserEndpoints
      .register
      .implement(userService.add)

  val getUser: Routes[Any, ApiError, None] =
    UserEndpoints
      .getUser
      .implement(userService.findByUsername)

  val all = (register ++ getUser).toApp
}

object UserEndpoints {

  val make = ZLayer.fromFunction(UserEndpoints.apply)

  private val register =
    Endpoint
      .post("register")
      .in[UserRegister]
      .out[UserResponse]
      .outError[ApiError](Status.BadRequest)

  private val getUser =
    Endpoint
      .get("user" / string("username"))
      .out[UserResponse]
      .outError[ApiError](Status.BadRequest)

}
