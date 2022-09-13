package testinstances

import domain.User
import services.generators.IdGenerator
import zio.*

case class UserGenerator(private val intGenerator: IdGenerator[Int]) {

  def generate(username: String): UIO[User] =
    for {
      id <- intGenerator.generate
      pwd <- Random.nextString(10)
    } yield User(username, pwd, id)
    
  def generate: UIO[User] =
    Random.nextString(10).flatMap(generate)

}

object UserGenerator {
  val instance: URLayer[IdGenerator[Int], UserGenerator] = ZLayer.fromFunction(UserGenerator.apply)

  def generate(username: String): RIO[UserGenerator, User] =
    ZIO.serviceWithZIO[UserGenerator](_.generate(username))

  def generate: RIO[UserGenerator, User] =
    ZIO.serviceWithZIO[UserGenerator](_.generate) 
}
