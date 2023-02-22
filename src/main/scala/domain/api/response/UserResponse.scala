package domain.api.response

import zio.json.JsonCodec
import zio.schema.{DeriveSchema, Schema}

case class UserResponse(
    username: String
) derives JsonCodec

object UserResponse {
  given Schema[UserResponse] = DeriveSchema.gen
}
