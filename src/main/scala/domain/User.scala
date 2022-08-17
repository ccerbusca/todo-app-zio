package domain

import zhttp.http.middleware.Auth.Credentials
import zio.json.*

import java.util.UUID

case class User(
  username: String,
  password: String,
  id: Int
) extends WithId[Int]

object User {
  given encoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]
}
