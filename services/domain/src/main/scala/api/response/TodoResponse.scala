package api.response

import zio.json.{DeriveJsonEncoder, JsonCodec, JsonEncoder}
import zio.schema.*

case class TodoResponse(
    title: String,
    content: String,
    completed: Boolean,
) derives JsonCodec, Schema
