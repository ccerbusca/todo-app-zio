package domain

import domain.api.response.{TodoResponse, toResponse}
import zio.json.*

import java.util.UUID

case class Todo(
  id: Int,
  parentId: Int,
  title: String,
  content: String,
  completed: Boolean = false
) extends WithId[Int]

object Todo {
  given jsonEncoder: JsonEncoder[Todo] = JsonEncoder[TodoResponse].contramap(_.toResponse)
}
