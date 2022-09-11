package domain.dto.response

import domain.Todo
import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class TodoResponse(
  title: String,
  content: String,
  completed: Boolean
)

object TodoResponse {
  given JsonEncoder[TodoResponse] = DeriveJsonEncoder.gen

  def fromTodo(todo: Todo): TodoResponse =
    TodoResponse(
      title = todo.title,
      content = todo.content,
      completed = todo.completed
    )
}

extension (todo: Todo) {
  def toResponse: TodoResponse = TodoResponse.fromTodo(todo)
}
