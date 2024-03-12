package db.entities

case class User(
    id: User.ID,
    username: String,
    password: String,
) extends WithId[User.ID]

object User {
  type ID = Long
}
