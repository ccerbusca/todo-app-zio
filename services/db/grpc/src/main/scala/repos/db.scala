package repos

import io.getquill.jdbczio.Quill
import io.getquill.{ PostgresDialect, SnakeCase }
import zio.*

import javax.sql.DataSource

object db {
  type QuillPostgres = Quill[PostgresDialect, SnakeCase]

  val postgresManual: URLayer[DataSource, QuillPostgres] =
    Quill.Postgres.fromNamingStrategy(SnakeCase)

  val postgresDefault: TaskLayer[QuillPostgres] =
    Quill.DataSource.fromPrefix("postgresConfig") >>> postgresManual

}
