package domain

import zio.json.*
import zio.schema.*

import java.util.UUID

case class User(
    id: User.ID,
    username: String,
    password: String,
) extends WithId[User.ID]

object User {
  type ID = Long
}
