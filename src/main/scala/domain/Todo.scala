package domain

import zio.json.*

import java.util.UUID

case class Todo(
  title: String,
  content: String,
  id: UUID
) extends WithId[UUID]

object Todo {
  given jsonEncoder: JsonEncoder[Todo] = DeriveJsonEncoder.gen[Todo]
}
