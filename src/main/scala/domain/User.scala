package domain

import zio.json.*
import zio.schema.*

import java.util.UUID

case class User(
    username: String,
    password: String,
    id: Int,
) extends WithId[User.ID]

object User {
  type ID = Int
}
