package domain.errors

enum ApiError extends RuntimeException {
  case NotFound extends ApiError
  case WrongAuthInfo extends ApiError
  case FailedInsert extends ApiError
  case MissingCredentials extends ApiError
  case MissingToken extends ApiError
  case Unauthorized extends ApiError
}
