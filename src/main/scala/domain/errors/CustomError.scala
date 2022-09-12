package domain.errors

enum CustomError extends RuntimeException {
  case NotFound extends CustomError
  case WrongAuthInfo extends CustomError
  case FailedInsert extends CustomError
  case MissingCredentials extends CustomError
  case MissingToken extends CustomError
  case Unauthorized extends CustomError
}
