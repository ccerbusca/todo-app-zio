package endpoints

import api.request.UserRegister
import api.response.UserResponse
import domain.errors.ApiError
import services.UserService
import zio.http.*
import zio.http.endpoint.*
import zio.*

case class UserEndpoints(userService: UserService) {

  private val register =
    UserEndpoints
      .register
      .implement(Handler.fromFunctionZIO(userService.add))

  val all: HttpApp[Any] = Routes(
    register
  ).toHttpApp

}

object UserEndpoints {

  val make: URLayer[UserService, UserEndpoints] = ZLayer.fromFunction(UserEndpoints.apply)

  private val register =
    Endpoint(Method.POST / "register")
      .in[UserRegister]
      .out[UserResponse]
      .outError[ApiError](Status.BadRequest)

}
