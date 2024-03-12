package api.domain

import io.github.arainko.ducktape.*
import users.user.User

extension (user: User) {

  def toDomain: Option[db.entities.User] =
    user.id.map { userId =>
      user
        .into[db.entities.User]
        .transform(Field.const(_.id, userId))
    }

}
