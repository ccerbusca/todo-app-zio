package api.request

import zio.schema.{DeriveSchema, Schema}

case class UserAuthenticate(
    username: String,
    password: String,
)

object UserAuthenticate {

  given Schema[UserAuthenticate] = DeriveSchema.gen
}
