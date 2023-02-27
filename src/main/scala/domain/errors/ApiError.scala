package domain.errors

import zio.http.Response
import zio.http.endpoint.{ EndpointMiddleware, RoutesMiddleware }
import zio.http.model.{ HttpError, Status }
import zio.schema.{ DeriveSchema, Schema }

enum ApiError(val status: Status) extends RuntimeException {
  case NotFound           extends ApiError(Status.NotFound)
  case WrongAuthInfo      extends ApiError(Status.BadRequest)
  case FailedInsert       extends ApiError(Status.InternalServerError)
  case FailedUpdate       extends ApiError(Status.InternalServerError)
  case MissingCredentials extends ApiError(Status.BadRequest)
  case MissingToken       extends ApiError(Status.Unauthorized)
  case Unauthorized       extends ApiError(Status.Unauthorized)
}

object ApiError {
  given Schema[ApiError] = DeriveSchema.gen
}
