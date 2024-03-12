package api.errors

import zio.json.JsonCodec
import zio.schema.*

enum ApiError derives JsonCodec, Schema {
  case NotFound           extends ApiError
  case UsernameTaken      extends ApiError
  case MissingCredentials extends ApiError
  case WrongAuthInfo      extends ApiError
  case MissingToken       extends ApiError
  case Unauthorized       extends ApiError
  case InternalError      extends ApiError
  case InvalidAuthHeader  extends ApiError
}
