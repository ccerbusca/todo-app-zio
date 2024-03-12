package functional

import api.request.UserRegister
import api.response.UserResponse
import auth.PasswordEncoder
import domain.errors.ApiError
import endpoints.UserEndpoints
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import repos.*
import services.*
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*

object UserEndpointsSpec extends BaseFunctionalTest {

  override def tests: Spec[db.QuillPostgres, Any] =
    suite("UserEndpointsSpec")(
      test("POST /register") {
        val userRegister = UserRegister("username", "password")
        val request      = Request.post(
          body = Body.fromString(userRegister.toJson),
          url = url"/register",
        )
        for {
          endpoints <- ZIO.serviceWith[UserEndpoints](_.all)
          response  <- endpoints.runZIO(request)
          body      <- response.body.asString
        } yield assertTrue(response.status.isSuccess, body == UserResponse("username").toJson)
      },
      test("POST /register - Username already taken") {
        val userRegister = UserRegister("username", "password")
        val request      = Request.post(
          body = Body.fromString(userRegister.toJson),
          url = url"/register",
        )
        for {
          _         <- ZIO.serviceWithZIO[UserRepo](_.add(userRegister))
          endpoints <- ZIO.serviceWith[UserEndpoints](_.all)
          response  <- endpoints.runZIO(request)
          body      <- response.body.asString
        } yield assertTrue(response.status.isSuccess, body == ApiError.UsernameTaken.toJson)
      },
    )
      .provideSome[db.QuillPostgres](
        UserEndpoints.make,
        PasswordEncoder.live,
        UserService.live,
        UserRepo.live,
      )

}
