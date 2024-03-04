package api.request

import zio.json.*
import zio.schema.{ DeriveSchema, Schema }

case class AddTodo(
    title: String,
    content: String,
) derives JsonCodec

object AddTodo {
  given Schema[AddTodo] = DeriveSchema.gen
}
