package api.endpoints

import api.errors.ApiError
import api.request.UserRegister
import api.response.UserResponse
import api.services.UserService
import zio.*
import zio.http.*
import zio.http.endpoint.*

case class UserEndpoints(userService: UserService) {

  private val register =
    UserEndpoints
      .register
      .implement(userService.add)

  val all: Routes[Any, Nothing] = Routes(
    register
  )

}

object UserEndpoints {

  val make: URLayer[UserService, UserEndpoints] = ZLayer.fromFunction(UserEndpoints.apply)

  private val register =
    Endpoint(Method.POST / "register")
      .in[UserRegister]
      .out[UserResponse]
      .outError[ApiError](Status.BadRequest)

}
