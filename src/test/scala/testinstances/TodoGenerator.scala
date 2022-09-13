package testinstances

import domain.{Todo, User}
import services.generators.IdGenerator
import zio.*

case class TodoGenerator(private val idGenerator: IdGenerator[Int], private val userGenerator: UserGenerator) {
  def generate(user: User): UIO[Todo] =
    for {
      id <- idGenerator.generate
      title <- Random.nextString(10)
      content <- Random.nextString(10)
    } yield Todo(id, user.id, title, content)
}

object TodoGenerator {
  val instance: URLayer[IdGenerator[Int] & UserGenerator, TodoGenerator] =
    ZLayer.fromFunction(TodoGenerator.apply)

  def generate(user: User): RIO[TodoGenerator, Todo] =
    ZIO.serviceWithZIO[TodoGenerator](_.generate(user))
}
