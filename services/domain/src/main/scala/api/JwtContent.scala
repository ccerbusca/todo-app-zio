package api

import zio.json.JsonCodec

case class JwtContent(
    id: Long,
    username: String,
) derives JsonCodec
