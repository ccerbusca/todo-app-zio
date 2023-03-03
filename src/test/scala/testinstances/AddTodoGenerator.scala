package testinstances

import domain.User
import domain.api.request.AddTodo
import domain.generators.Generator
import zio.*

case class AddTodoGenerator() extends Generator[AddTodo] {

  override def generate: UIO[AddTodo] =
    for {
      title   <- Random.nextString(10)
      content <- Random.nextString(10)
    } yield AddTodo(title, content)

}

object AddTodoGenerator {

  val instance: ULayer[AddTodoGenerator] =
    ZLayer.succeed(AddTodoGenerator())

  def generate: RIO[AddTodoGenerator, AddTodo] =
    ZIO.serviceWithZIO[AddTodoGenerator](_.generate)

}
