package endpoints

import api.request.UserAuthenticate
import domain.errors.ApiError
import services.{ AuthService, JwtService }
import zio.http.*
import zio.http.endpoint.*
import zio.*

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
