package testinstances

import domain.{Todo, User}
import services.generators.Generator
import zio.*

case class TodoGenerator(
  private val idGenerator: Generator[Int],
  private val userGenerator: UserGenerator
) extends Generator[Todo] {
  def generate(user: User): UIO[Todo] =
    for {
      id <- idGenerator.generate
      title <- Random.nextString(10)
      content <- Random.nextString(10)
    } yield Todo(id, user.id, title, content)

  override def generate: UIO[Todo] =
    userGenerator.generate.flatMap(generate)
}

object TodoGenerator {
  val instance: URLayer[Generator[Int] & UserGenerator, TodoGenerator] =
    ZLayer.fromFunction(TodoGenerator.apply)

  def generate(user: User): RIO[TodoGenerator, Todo] =
    ZIO.serviceWithZIO[TodoGenerator](_.generate(user))
}
