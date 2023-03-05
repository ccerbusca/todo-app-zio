package domain.errors

import zio.http.Response
import zio.http.endpoint.{EndpointMiddleware, RoutesMiddleware}
import zio.http.model.{HttpError, Status}
import zio.json.JsonCodec
import zio.schema.{DeriveSchema, Schema}

enum ApiError(val status: Status) derives JsonCodec {
  case NotFound           extends ApiError(Status.NotFound)
  case UsernameTaken      extends ApiError(Status.BadRequest)
  case MissingCredentials extends ApiError(Status.Unauthorized)
  case WrongAuthInfo      extends ApiError(Status.Unauthorized)
  case MissingToken       extends ApiError(Status.Unauthorized)
  case Unauthorized       extends ApiError(Status.Unauthorized)
}

object ApiError {
  given Schema[ApiError] = DeriveSchema.gen
}
