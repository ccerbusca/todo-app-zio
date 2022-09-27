package unit

import auth.{$$, AuthContext, AuthMiddleware}
import unit.AuthMiddlewareSpec.{suite, suiteAll, test}
import zhttp.http.*
import zhttp.http.middleware.Auth.Credentials
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{ZIOSpecDefault, assertZIO}

object AuthMiddlewareSpec extends ZIOSpecDefault {

  val basicAuthHeaderSuccess: Headers = Headers.basicAuthorizationHeader("test", "test")
  val basicAuthHeaderFailure: Headers = Headers.basicAuthorizationHeader("test", "incorrect")
  val bearerAuthHeaderSuccess: Headers = Headers.bearerAuthorizationHeader("test")
  val bearerAuthHeaderFailure: Headers = Headers.bearerAuthorizationHeader("bad")
  val successfulResponse = "Authenticated"
  val failedResponse = "failed"
  val missingResponse = "missing"

  override def spec =
    suite("AuthMiddlewareSpec")(
      suiteAll("customBasicAuth") {
        val middleware = AuthMiddleware.customBasicAuth(missingResponse) {
          case Credentials("test", "test") => ZIO.succeed(successfulResponse)
          case _ => ZIO.fail(failedResponse)
        }

        val testRoute = Http.collect[AuthContext[String]] {
          case _ $$ context => Response.text(context)
        } @@ middleware

        val bodyApp = testRoute.mapZIO(_.body.asString)
        val statusApp = testRoute.status

        test("context is passed on to route if basic auth succeeds") {
          assertZIO(bodyApp(Request().addHeaders(basicAuthHeaderSuccess)))(equalTo(successfulResponse))
        }

        test("fail if basic auth credentials do not match") {
          assertZIO(statusApp(Request().addHeaders(basicAuthHeaderFailure)))(equalTo(Status.Unauthorized))
        }

        test("fail if basic auth credentials are missing") {
          assertZIO(statusApp(Request()))(equalTo(Status.Unauthorized))
        }

      },

      suiteAll("customBearerAuth") {
        val middleware = AuthMiddleware.customBearerAuth(missingResponse) {
          case "test" => ZIO.succeed(successfulResponse)
          case _ => ZIO.fail(failedResponse)
        }

        val testRoute = Http.collect[AuthContext[String]] {
          case _ $$ context => Response.text(context)
        } @@ middleware

        val bodyApp = testRoute.mapZIO(_.body.asString)
        val statusApp = testRoute.status

        test("context is passed on to route if bearer auth succeeds") {
          assertZIO(bodyApp(Request().addHeaders(bearerAuthHeaderSuccess)))(equalTo(successfulResponse))
        }

        test("fail if bearer auth token does not match") {
          assertZIO(statusApp(Request().addHeaders(bearerAuthHeaderFailure)))(equalTo(Status.Unauthorized))
        }

        test("fail if bearer auth token is missing") {
          assertZIO(statusApp(Request()))(equalTo(Status.Unauthorized))
        }

      }
    )

}
