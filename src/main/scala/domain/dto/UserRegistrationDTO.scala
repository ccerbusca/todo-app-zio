package domain.dto

import zio.json.*

case class UserRegistrationDTO(
  username: String,
  password: String
)

object UserRegistrationDTO {
  given jsonCodec: JsonCodec[UserRegistrationDTO] = DeriveJsonCodec.gen[UserRegistrationDTO]
}
