package functional

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import zio.test.{ Spec, ZIOSpecDefault }

import javax.sql.DataSource

trait BaseFunctionalTest extends ZIOSpecDefault {

  override def spec: Spec[Any, Any] =
    (tests @@ DbMigrationAspect.migrate()())
      .provide(
        ZPostgreSQLContainer.Settings.default,
        ZPostgreSQLContainer.live,
        Quill.Postgres.fromNamingStrategy(io.getquill.SnakeCase),
      )

  def tests: Spec[db.QuillPostgres, Any]

}
