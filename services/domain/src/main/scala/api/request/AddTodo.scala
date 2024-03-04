package api.request

import zio.json.*
import zio.schema.*

case class AddTodo(
    title: String,
    content: String,
) derives JsonCodec, Schema