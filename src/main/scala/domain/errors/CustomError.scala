package domain.errors

enum CustomError extends RuntimeException {
  case NotFound extends CustomError
  case WrongCredentials extends CustomError
}
