package domain

import domain.dto.response.TodoResponse
import domain.relationships.{Many, One}
import zio.json.*
import domain.dto.response.toResponse

import java.util.UUID

case class Todo(
  id: Int,
  parentId: Int,
  title: String,
  content: String,
  completed: Boolean = false
) extends WithId[Int] with One[User, User.ID]

object Todo {
  given jsonEncoder: JsonEncoder[Todo] = JsonEncoder[TodoResponse].contramap(_.toResponse)
}
