package api.request

import zio.json.*
import zio.schema.*

case class UserRegister(
    username: String,
    password: String,
) derives JsonCodec, Schema
