package endpoints

import api.request.UserAuthenticate
import domain.errors.ApiError
import services.{AuthService, JwtService}
import zio.http.endpoint.*
import zio.http.model.Status
import zio.{URLayer, ZIO, ZLayer}

case class AuthEndpoints(authService: AuthService, jwtService: JwtService) {

  val login =
    AuthEndpoints
      .loginEndpoint
      .implement { userPayload =>
        authService
          .authenticate(userPayload)
          .flatMap(jwtService.encode)
      }

  val all = login.toApp
}

object AuthEndpoints {

  val make: URLayer[AuthService & JwtService, AuthEndpoints] = ZLayer.fromFunction(AuthEndpoints.apply)

  private val loginEndpoint =
    Endpoint
      .post("login")
      .in[UserAuthenticate]
      .out[String]
      .outError[ApiError](Status.Unauthorized)

}
