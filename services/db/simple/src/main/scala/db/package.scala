package db

import io.getquill.jdbczio.Quill
import io.getquill.{PostgresDialect, SnakeCase}
import zio.{TaskLayer, URLayer}

import javax.sql.DataSource

type QuillPostgres = Quill[PostgresDialect, SnakeCase]

val postgresManual: URLayer[DataSource, QuillPostgres] =
  Quill.Postgres.fromNamingStrategy(SnakeCase)

val postgresDefault: TaskLayer[QuillPostgres] =
  Quill.DataSource.fromPrefix("postgresConfig") >>> postgresManual
