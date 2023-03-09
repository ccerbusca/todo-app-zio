package api

import zio.json.JsonCodec

case class JwtContent(
    id: Int,
    username: String,
) derives JsonCodec
