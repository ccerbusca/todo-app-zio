package domain.errors

enum CustomError extends RuntimeException {
  case UserNotFound extends CustomError
  case WrongCredentials extends CustomError
}
