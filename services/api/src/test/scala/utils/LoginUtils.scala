package utils

import domain.User
import repos.UserRepo
import services.JwtService
import utils.testinstances.UserRegisterGenerator
import zio.*

object LoginUtils {

  def testUser: RIO[UserRegisterGenerator & UserRepo, User] = for {
    userGenerate <- UserRegisterGenerator.generate
    user         <- ZIO.serviceWithZIO[UserRepo](_.add(userGenerate))
  } yield user

  def testToken: RIO[UserRegisterGenerator & JwtService & UserRepo, String] = for {
    user  <- testUser
    token <- testToken(user)
  } yield token

  def testToken(user: User): ZIO[JwtService, Nothing, String] =
    ZIO.serviceWithZIO[JwtService](_.encode(user))

}
