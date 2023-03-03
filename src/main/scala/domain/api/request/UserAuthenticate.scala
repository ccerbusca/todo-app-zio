package domain.api.request

import zio.http.middleware.Auth.Credentials
import zio.schema.{DeriveSchema, Schema}

case class UserAuthenticate(
    username: String,
    password: String,
)

object UserAuthenticate {
  
  given Schema[UserAuthenticate] = DeriveSchema.gen
  def fromCredentials(c: Credentials): UserAuthenticate = UserAuthenticate(c.uname, c.upassword)
}
