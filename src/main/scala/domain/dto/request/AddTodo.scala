package domain.dto.request

import zio.json.*

case class AddTodo(
  title: String,
  content: String,
)

object AddTodo {
  given decoder: JsonDecoder[AddTodo] = DeriveJsonDecoder.gen[AddTodo]
}
