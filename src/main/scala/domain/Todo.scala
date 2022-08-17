package domain

import domain.relationships.{One, Many}
import zio.json.*

import java.util.UUID

case class Todo(
  title: String,
  content: String,
  id: Int,
  parentId: Int
) extends WithId[Int] with One[User, Int]

object Todo {
  given jsonEncoder: JsonEncoder[Todo] = DeriveJsonEncoder.gen[Todo]
}
