package functional

import api.response.UserResponse
import auth.PasswordEncoder
import endpoints.UserEndpoints
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import repos.*
import services.*
import zio.*
import zio.http.*
import zio.http.endpoint.EndpointExecutor
import zio.json.*
import zio.test.*

object UserEndpointsSpec extends ZIOSpecDefault {

  override def spec =
    (suite("UserEndpointsSpec")(
      test("POST /register") {
        val userRegister = UserRegister("username", "password")
        val request      = Request.post(
          Body.fromString(userRegister.toJson),
          URL.fromString("/register").toOption.get,
        )
        for {
          endpoints <- ZIO.serviceWith[UserEndpoints](_.all)
          response  <- endpoints.runZIO(request)
          body      <- response.body.asString
        } yield assertTrue(response.status.isSuccess) && assertTrue(
          body == UserResponse("username").toJson
        )
      },
      test("POST /register - Username already taken") {
        val userRegister = UserRegister("username", "password")
        val request      = Request.post(
          Body.fromString(userRegister.toJson),
          URL.fromString("/register").toOption.get,
        )
        for {
          _         <- ZIO.serviceWithZIO[UserRepo](_.add(userRegister))
          endpoints <- ZIO.serviceWith[UserEndpoints](_.all)
          response  <- endpoints.runZIO(request)
          body      <- response.body.asString
        } yield assertTrue(response.status.isSuccess) && assertTrue(
          body == ApiError.UsernameTaken.toJson
        )
      },
    ) @@ DbMigrationAspect.migrate()())
      .provide(
        UserEndpoints.make,
        PasswordEncoder.live,
        UserService.live,
        UserRepo.live,
        ZPostgreSQLContainer.Settings.default,
        ZPostgreSQLContainer.live,
        Quill.Postgres.fromNamingStrategy(io.getquill.SnakeCase),
      )

}
