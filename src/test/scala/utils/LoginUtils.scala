package utils

import domain.User
import domain.errors.ApiError
import repos.UserRepo
import services.{JwtService, UserService}
import utils.testinstances.UserRegisterGenerator
import zio.ZIO

object LoginUtils {

  def testUser: ZIO[UserRegisterGenerator & UserRepo, ApiError, User] = for {
    userGenerate <- UserRegisterGenerator.generate
    user         <- ZIO.serviceWithZIO[UserRepo](_.add(userGenerate))
  } yield user

  def testToken: ZIO[UserRegisterGenerator & JwtService & UserRepo, ApiError, String] = for {
    user  <- testUser
    token <- testToken(user)
  } yield token
  
  def testToken(user: User) = ZIO.serviceWithZIO[JwtService](_.encode(user))

}
