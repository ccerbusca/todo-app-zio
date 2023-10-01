ThisBuild / scalaVersion     := "3.2.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "todo"
ThisBuild / organizationName := "todo"

val zioVersion     = "2.0.10"
val zioHttpVersion = "0.0.4+29-eb3bab9d-SNAPSHOT"

resolvers ++= Resolver.sonatypeOssRepos("releases") ++ Resolver.sonatypeOssRepos("snapshots")

lazy val root = (project in file("."))
  .settings(
    name    := "ZIOTest",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "dev.zio"               %% "zio"                               % zioVersion,
      "dev.zio"               %% "zio-streams"                       % zioVersion,
      "dev.zio"               %% "zio-concurrent"                    % zioVersion,
      "dev.zio"               %% "zio-test"                          % zioVersion % Test,
      "dev.zio"               %% "zio-test-sbt"                      % zioVersion % Test,
      "dev.zio"               %% "zio-test-magnolia"                 % zioVersion % Test,
      "dev.zio"               %% "zio-json"                          % "0.5.0",
      "dev.zio"               %% "zio-http"                          % zioHttpVersion,
      "dev.zio"               %% "zio-http-testkit"                  % zioHttpVersion,
      "io.getquill"           %% "quill-jdbc-zio"                    % "4.7.3",
      "io.github.arainko"     %% "ducktape"                          % "0.1.3",
      "com.github.jwt-scala"  %% "jwt-zio-json"                      % "9.2.0",
      "io.github.scottweaver" %% "zio-2-0-testcontainers-postgresql" % "0.10.0"   % Test,
      "io.github.scottweaver" %% "zio-2-0-db-migration-aspect"       % "0.10.0"   % Test,
      "com.github.ksuid"       % "ksuid"                             % "1.1.2",
      "org.postgresql"         % "postgresql"                        % "42.6.0",
      "com.password4j"         % "password4j"                        % "1.7.0",
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalacOptions ++= Seq(
      "-no-indent"
    ),
  )

Test / fork := true

//Flyway configuration
enablePlugins(FlywayPlugin)

flywayLocations += "db/migration"

flywayUrl      := "jdbc:postgresql://localhost:15432/test"
flywayUser     := "postgres"
flywayPassword := "admin"

Test / flywayUrl      := "jdbc:postgresql://localhost/test"
Test / flywayUser     := "postgres"
Test / flywayPassword := "admin"

//Dockerization
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

Docker / maintainer := "ccerbusca"
