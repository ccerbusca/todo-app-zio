package domain

import domain.relationships.{One, Many}
import zio.json.*

import java.util.UUID

case class Todo(
  title: String,
  content: String,
  id: UUID,
  parentId: UUID
) extends WithId[UUID] with One[User, UUID]

object Todo {
  given jsonEncoder: JsonEncoder[Todo] = DeriveJsonEncoder.gen[Todo]
}
