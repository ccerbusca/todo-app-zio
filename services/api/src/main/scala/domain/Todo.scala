package domain

import zio.json.*
import zio.schema.{DeriveSchema, Schema}

import java.util.UUID

case class Todo(
    id: Todo.ID,
    parentId: User.ID,
    title: String,
    content: String,
    completed: Boolean = false,
) extends WithId[Todo.ID]

object Todo {
  type ID = Long
}
