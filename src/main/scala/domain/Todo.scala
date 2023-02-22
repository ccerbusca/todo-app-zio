package domain

import domain.api.response.{TodoResponse, toResponse}
import zio.json.*
import zio.schema.{DeriveSchema, Schema}

import java.util.UUID

case class Todo(
    id: Int,
    parentId: Int,
    title: String,
    content: String,
    completed: Boolean = false,
) extends WithId[Todo.ID]

object Todo {
  type ID = Int

  given jsonEncoder: JsonEncoder[Todo] = JsonEncoder[TodoResponse].contramap(_.toResponse)
  given Schema[Todo]                   = DeriveSchema.gen
}
