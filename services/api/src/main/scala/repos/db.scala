package repos

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

import javax.sql.DataSource

object db {
  val postgresManual: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase.type]] =
    Quill.Postgres.fromNamingStrategy(SnakeCase)
  val postgresDefault: ZLayer[Any, Throwable, Quill.Postgres[SnakeCase.type]] =
    Quill.DataSource.fromPrefix("postgresConfig") >>> postgresManual
}
