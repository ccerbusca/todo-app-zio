package functional

import auth.PasswordEncoder
import domain.api.request.UserRegister
import domain.api.response.UserResponse
import domain.generators.Generator
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
      suite("/register")(
        test("success") {
          val userRegister = UserRegister("username", "password")
          val request      = Request.post(
            Body.fromString(userRegister.toJson),
            URL.fromString("/register").toOption.get,
          )
          for {
            endpoints <- UserEndpoints.make
            response  <- endpoints.runZIO(request)
            body      <- response.body.asString
          } yield assertTrue(response.status.isSuccess) && assertTrue(
            body == UserResponse("username").toJson
          )
        }
      )
    ) @@ DbMigrationAspect.migrate()())
      .provide(
        PasswordEncoder.live,
        Generator.int(),
        UserService.live,
        UserRepo.live,
        ZPostgreSQLContainer.Settings.default,
        ZPostgreSQLContainer.live,
        Quill.Postgres.fromNamingStrategy(io.getquill.SnakeCase),
      )

}
