package utils.testinstances

import zio.*

case class UserRegisterGenerator() extends Generator[UserRegister] {

  override def generate: UIO[UserRegister] =
    Random.nextString(10).flatMap(generate)

  def generate(username: String): UIO[UserRegister] =
    for {
      pwd <- Random.nextString(10)
    } yield UserRegister(username, pwd)

}

object UserRegisterGenerator {
  val instance = ZLayer.succeed(UserRegisterGenerator())

  def generate(username: String): URIO[UserRegisterGenerator, UserRegister] =
    ZIO.serviceWithZIO[UserRegisterGenerator](_.generate(username))

  def generate: URIO[UserRegisterGenerator, UserRegister] =
    ZIO.serviceWithZIO[UserRegisterGenerator](_.generate)

}
