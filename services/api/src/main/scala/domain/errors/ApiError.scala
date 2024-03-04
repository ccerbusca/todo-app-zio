package domain.errors

import zio.http.Status
import zio.json.JsonCodec
import zio.schema.*

enum ApiError(val status: Status) derives JsonCodec, Schema {
  case NotFound           extends ApiError(Status.NotFound)
  case UsernameTaken      extends ApiError(Status.BadRequest)
  case MissingCredentials extends ApiError(Status.Unauthorized)
  case WrongAuthInfo      extends ApiError(Status.Unauthorized)
  case MissingToken       extends ApiError(Status.Unauthorized)
  case Unauthorized       extends ApiError(Status.Unauthorized)
  case InternalError      extends ApiError(Status.InternalServerError)
  case InvalidAuthHeader  extends ApiError(Status.BadRequest)
}
