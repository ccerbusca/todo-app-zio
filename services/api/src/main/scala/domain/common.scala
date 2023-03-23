package domain

import domain.User as DUser
import io.github.arainko.ducktape.*
import users.user.User

extension (user: User) {

  def toDomain: Option[DUser] =
    user.id.map { userId =>
      user
        .into[DUser]
        .transform(Field.const(_.id, userId))
    }

}
