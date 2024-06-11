package api.endpoints

import api.errors.ApiError
import api.request.UserAuthenticate
import api.services.{AuthService, JwtService}
import zio.*
import zio.http.*
import zio.http.endpoint.*

case class AuthEndpoints(authService: AuthService, jwtService: JwtService) {

  private val login =
    AuthEndpoints
      .loginEndpoint
      .implement { userPayload =>
        authService
          .authenticate(userPayload)
          .flatMap(jwtService.encode)
      }

  val all: Routes[Any, Nothing] = Routes(
    login
  )

}

object AuthEndpoints {

  val make: URLayer[AuthService & JwtService, AuthEndpoints] = ZLayer.fromFunction(AuthEndpoints.apply)

  private val loginEndpoint =
    Endpoint(Method.POST / "login")
      .in[UserAuthenticate]
      .out[String]
      .outError[ApiError](Status.Unauthorized)

}
