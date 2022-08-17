package auth

import com.password4j.Password
import zio.*

trait PasswordEncoder {
  def encode(password: String): String
  def verify(password: String, hashedPassword: String): Boolean
}

case class PasswordEncoderLive() extends PasswordEncoder {
  override def encode(password: String): String =
    Password
      .hash(password)
      .addRandomSalt()
      .withArgon2()
      .getResult

  override def verify(password: String, hashedPassword: String): Boolean =
    Password
      .check(password, hashedPassword)
      .withArgon2()
}

object PasswordEncoder {
  val live: ULayer[PasswordEncoder] = ZLayer.succeed(PasswordEncoderLive())
  
  def encode(password: String): RIO[PasswordEncoder, String] =
    ZIO.serviceWith[PasswordEncoder](_.encode(password))

  def verify(password: String, hashedPassword: String): RIO[PasswordEncoder, Boolean] =
    ZIO.serviceWith[PasswordEncoder](_.verify(password, hashedPassword))
}
