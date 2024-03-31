package api.endpoints

import api.errors.ApiError
import api.request.UserAuthenticate
import api.services.{ AuthService, JwtService }
import zio.*
import zio.http.*
import zio.http.endpoint.*

case class AuthEndpoints(authService: AuthService, jwtService: JwtService) {

  private val login =
    AuthEndpoints
      .loginEndpoint
      .implement {
        Handler.fromFunctionZIO[UserAuthenticate] { userPayload =>
          authService
            .authenticate(userPayload)
            .flatMap(jwtService.encode)
        }
      }

  val all: HttpApp[Any] = Routes(
    login
  ).toHttpApp

}

object AuthEndpoints {

  val make: URLayer[AuthService & JwtService, AuthEndpoints] = ZLayer.fromFunction(AuthEndpoints.apply)

  private val loginEndpoint =
    Endpoint(Method.POST / "login")
      .in[UserAuthenticate]
      .out[String]
      .outError[ApiError](Status.Unauthorized)

}
