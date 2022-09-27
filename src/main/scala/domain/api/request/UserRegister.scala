package domain.api.request

import zio.json.*

case class UserRegister(
  username: String,
  password: String
)

object UserRegister {
  given decoder: JsonDecoder[UserRegister] = DeriveJsonDecoder.gen[UserRegister]
}
