package domain.dto

import zhttp.http.middleware.Auth.Credentials

case class AuthenticateDTO(
  username: String,
  password: String
)

object AuthenticateDTO {
  def fromCredentials(c: Credentials): AuthenticateDTO = AuthenticateDTO(c.uname, c.upassword)
}
