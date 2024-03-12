package api.request

import zio.schema.*

case class UserAuthenticate(
    username: String,
    password: String,
) derives Schema
