ThisBuild / scalaVersion     := "3.1.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "todo"
ThisBuild / organizationName := "todo"

val zioVersion = "2.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "ZIOTest",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "dev.zio"          %% "zio"                % zioVersion,
      "dev.zio"          %% "zio-concurrent"     % zioVersion,
      "dev.zio"          %% "zio-test"           % zioVersion % Test,
      "dev.zio"          %% "zio-test-sbt"       % zioVersion % Test,
      "dev.zio"          %% "zio-test-magnolia"  % zioVersion % Test,
      "dev.zio"          %% "zio-json"           % "0.3.0-RC10",
      "io.d11"           %% "zhttp"              % "2.0.0-RC10",
      "io.d11"           %% "zhttp-test"         % "2.0.0-RC9" % Test,
      "io.getquill"      %% "quill-jdbc-zio"     % "4.2.0",
      "com.github.ksuid"  % "ksuid"              % "1.1.1",
      "org.postgresql"    % "postgresql"         % "42.4.1"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalacOptions += "-no-indent"
  )


//Flyway configuration
enablePlugins(FlywayPlugin)

flywayLocations += "db/migrations"

flywayUrl := "jdbc:postgresql://localhost:15432/test?createDatabaseIfNotExist=true"
flywayUser := "postgres"
flywayPassword := "admin"

Test / flywayUrl := "jdbc:postgresql://localhost/test"
Test / flywayUser := "postgres"
Test / flywayPassword := "admin"
