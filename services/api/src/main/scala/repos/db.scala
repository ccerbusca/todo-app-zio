package repos

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill

object db {
  val postgresManual  = Quill.Postgres.fromNamingStrategy(SnakeCase)
  val postgresDefault = Quill.DataSource.fromPrefix("postgresConfig") >>> postgresManual
}
