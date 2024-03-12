package api.response

import zio.json.JsonCodec
import zio.schema.*

case class UserResponse(
    username: String
) derives JsonCodec, Schema