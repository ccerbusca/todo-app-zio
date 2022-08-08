package domain

import zhttp.http.middleware.Auth.Credentials
import zio.json.*

import java.util.UUID

case class User(
  username: String,
  password: String,
  id: UUID
) extends WithId[UUID]

object User {
  given encoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]
  given decoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]
}
