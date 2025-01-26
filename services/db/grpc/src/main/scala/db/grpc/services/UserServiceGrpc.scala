package db.grpc.services

import api.request.UserRegister
import db.entities
import db.repos.UserRepo
import io.github.arainko.ducktape.*
import io.grpc.{ Status, StatusException }
import scalapb.UnknownFieldSet
import users.user.ZioUser.UserService
import users.user.{ User, Username }
import zio.*

case class UserServiceGrpc(userRepo: UserRepo) extends UserService {
  import UserServiceGrpc.*

  override def addUser(request: User): IO[StatusException, User] =
    userRepo
      .add(request.to[UserRegister])
      .mapBoth(_ => StatusException(Status.INTERNAL), _.toResponse)

  override def getUserByUsername(request: Username): IO[StatusException, User] =
    userRepo
      .findByUsername(request.username)
      .mapBoth(_ => StatusException(Status.NOT_FOUND), _.toResponse)

}

object UserServiceGrpc {

  val make: ZLayer[db.QuillPostgres, Nothing, UserServiceGrpc] =
    ZLayer.makeSome[db.QuillPostgres, UserServiceGrpc](
      ZLayer.fromFunction(UserServiceGrpc.apply),
      UserRepo.live,
    )

  extension (u: entities.User) {

    def toResponse: User =
      u.into[User]
        .transform(Field.const(_.unknownFields, UnknownFieldSet.empty))

  }

}
