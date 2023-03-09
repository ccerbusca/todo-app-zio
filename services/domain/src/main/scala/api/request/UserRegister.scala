package api.request

import zio.json.*
import zio.schema.{DeriveSchema, Schema}

case class UserRegister(
    username: String,
    password: String,
) derives JsonCodec

object UserRegister {
  given Schema[UserRegister] = DeriveSchema.gen
}
