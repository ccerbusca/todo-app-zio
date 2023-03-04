package domain.api.response

import domain.Todo
import zio.json.{DeriveJsonEncoder, JsonCodec, JsonEncoder}
import zio.schema.{DeriveSchema, Schema}

case class TodoResponse(
    title: String,
    content: String,
    completed: Boolean,
) derives JsonCodec

object TodoResponse {
  given Schema[TodoResponse] = DeriveSchema.gen

  def fromTodo(todo: Todo): TodoResponse =
    TodoResponse(
      title = todo.title,
      content = todo.content,
      completed = todo.completed,
    )

}

extension (todo: Todo) {
  def toResponse: TodoResponse = TodoResponse.fromTodo(todo)
}
