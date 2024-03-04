package services

import io.grpc.{ Status, StatusException }
import repos.{ db, UserRepo }
import users.user.{ User, Username }
import users.user.ZioUser.UserService
import zio.{ IO, ZIO, ZLayer }
import io.github.arainko.ducktape.*
import scalapb.UnknownFieldSet

case class UserServiceGrpc(userRepo: UserRepo) extends UserService {
  import UserServiceGrpc.*

  override def addUser(request: User): IO[StatusException, User] =
    userRepo
      .add(request)
      .mapBoth(_ => StatusException(Status.INTERNAL), _.toResponse)

  override def getUserByUsername(request: Username): IO[StatusException, User] =
    userRepo
      .findByUsername(request.username)
      .some
      .mapBoth(_ => StatusException(Status.NOT_FOUND), _.toResponse)

}

object UserServiceGrpc {

  val make = ZLayer.makeSome[db.QuillPostgres, UserServiceGrpc](
    ZLayer.fromFunction(UserServiceGrpc.apply),
    UserRepo.live,
  )

  extension (u: entities.User) {

    def toResponse: User =
      u.into[User]
        .transform(Field.const(_.unknownFields, UnknownFieldSet.empty))

  }

}
