package services

import io.grpc.Status
import repos.{ db, UserRepo }
import users.user.{ User, Username }
import users.user.ZioUser.UserService
import zio.{ IO, ZIO, ZLayer }
import io.github.arainko.ducktape.*
import scalapb.UnknownFieldSet

case class UserServiceGrpc(userRepo: UserRepo) extends UserService {
  import UserServiceGrpc.*

  override def addUser(request: User): IO[Status, User] =
    userRepo
      .add(request)
      .mapError(_ => Status.INTERNAL)
      .map(_.toResponse)

  override def getUserByUsername(request: Username): IO[Status, User] =
    userRepo
      .findByUsername(request.username)
      .some
      .map(_.toResponse)
      .mapError(_ => Status.NOT_FOUND)

}

object UserServiceGrpc {

  val make = ZLayer.makeSome[db.QuillPostgres, UserServiceGrpc](
    ZLayer.fromFunction(UserServiceGrpc.apply),
    UserRepo.live,
  )

  extension (u: entities.User) {

    def toResponse =
      u.into[User]
        .transform(Field.const(_.unknownFields, UnknownFieldSet.empty))

  }

}
