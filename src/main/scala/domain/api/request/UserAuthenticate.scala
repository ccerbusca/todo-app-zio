package domain.api.request

import zhttp.http.middleware.Auth.Credentials

case class UserAuthenticate(
  username: String,
  password: String
)

object UserAuthenticate {
  def fromCredentials(c: Credentials): UserAuthenticate = UserAuthenticate(c.uname, c.upassword)
}
