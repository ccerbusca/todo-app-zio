package domain.api.response

import domain.Todo
import zio.json.{ DeriveJsonEncoder, JsonCodec, JsonEncoder }
import zio.schema.{ DeriveSchema, Schema }

case class TodoResponse(
    title: String,
    content: String,
    completed: Boolean,
) derives JsonCodec

object TodoResponse {
  given Schema[TodoResponse] = DeriveSchema.gen

}
