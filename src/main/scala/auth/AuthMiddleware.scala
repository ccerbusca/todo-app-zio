package auth

import domain.{User, WithId}
import io.netty.handler.codec.http.HttpHeaderNames
import zhttp.http.Middleware
import zhttp.service.*
import zhttp.http.*
import zhttp.http.Headers.{BasicSchemeName, BearerSchemeName}
import zhttp.http.middleware.Auth.Credentials
import zio.*

import java.util.UUID

object AuthMiddleware {
  def customBasicAuth[R, E, A](error: E)(
    verify: Credentials => ZIO[R, E, A]
  ): Middleware[R, E, AuthContext[A], Response, Request, Response] =
    customAuthMiddleware(
      extract = headers => headers.basicAuthorizationCredentials,
      verify  = verify,
      headers = Headers(HttpHeaderNames.WWW_AUTHENTICATE, BasicSchemeName),
      error   = error
    )

  def customBearerAuth[R, E, A](error: E)(
    verify: String => ZIO[R, E, A]
  ): Middleware[R, E, AuthContext[A], Response, Request, Response] =
    customAuthMiddleware(
      extract = headers => headers.bearerToken,
      verify  = verify,
      headers = Headers(HttpHeaderNames.WWW_AUTHENTICATE, BearerSchemeName),
      error   = error
    )

  private def customAuthMiddleware[R, E, A, Cred](
    extract: Headers => Option[Cred],
    verify: Cred => ZIO[R, E, A],
    headers: Headers,
    error: E
  ): Middleware[R, E, AuthContext[A], Response, Request, Response] =
    authMiddleware(
      headers => extract(headers) match {
        case Some(credentials) => verify(credentials)
        case None => ZIO.fail(error)
      },
      headers,
    )

  private def authMiddleware[R, E, A](
    verify: Headers => ZIO[R, E, A],
    responseHeaders: Headers = Headers.empty,
    responseStatus: Status = Status.Unauthorized,
  ): Middleware[R, E, AuthContext[A], Response, Request, Response] =
    Middleware.codecZIO[Request, Response](
      request =>
        verify(request.headers)
          .map(AuthContext(request, _)),
      response =>
        ZIO.succeed(response)
    ) <> Middleware.fromHttp(Http.status(responseStatus).addHeaders(responseHeaders))

}

case class AuthContext[T](
  request: Request,
  contextInfo: T
)