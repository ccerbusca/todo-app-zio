ThisBuild / scalaVersion     := "3.3.0"
ThisBuild / organization     := "todo"
ThisBuild / organizationName := "todo"
ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("releases") ++ Resolver.sonatypeOssRepos("snapshots")

def stdSettings(moduleName: String) = Seq(
  name        := moduleName,
  version     := "0.1.0",
  Test / fork := true,
  scalacOptions ++= Seq(
    "-no-indent"
  ),
)

lazy val root = (project in file("."))
  .settings(
    name           := "TodoApp",
    publish / skip := true,
  )
  .aggregate(domain, api, db, protos)

lazy val api = (project in file("services/api"))
  .dependsOn(protos, domain)
  .enablePlugins(FlywayPlugin, JavaAppPackaging, DockerPlugin)
  .settings(stdSettings("api"))
  .settings(flywaySettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio"               %% "zio"                               % V.zio,
      "dev.zio"               %% "zio-test"                          % V.zio               % Test,
      "dev.zio"               %% "zio-test-sbt"                      % V.zio               % Test,
      "dev.zio"               %% "zio-test-magnolia"                 % V.zio               % Test,
      "dev.zio"               %% "zio-json"                          % V.zioJson,
      "dev.zio"               %% "zio-http"                          % V.zioHttp,
      "dev.zio"               %% "zio-http-testkit"                  % V.zioHttp,
      "io.getquill"           %% "quill-jdbc-zio"                    % V.quill,
      "io.github.arainko"     %% "ducktape"                          % V.ducktape,
      "com.github.jwt-scala"  %% "jwt-zio-json"                      % V.jwtScala,
      "io.github.scottweaver" %% "zio-2-0-testcontainers-postgresql" % V.zioTestcontainers % Test,
      "io.github.scottweaver" %% "zio-2-0-db-migration-aspect"       % V.zioTestcontainers % Test,
      "com.github.ksuid"       % "ksuid"                             % V.ksuid,
      "org.postgresql"         % "postgresql"                        % V.postgres,
      "com.password4j"         % "password4j"                        % V.password4j,
      "io.grpc"                % "grpc-netty"                        % V.grpcNetty,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  )

lazy val db = (project in file("services/db"))
  .dependsOn(protos, domain)
  .enablePlugins(FlywayPlugin, JavaAppPackaging, DockerPlugin)
  .settings(stdSettings("db"))
  .settings(flywaySettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio"               %% "zio"                               % V.zio,
      "dev.zio"               %% "zio-test"                          % V.zio               % Test,
      "dev.zio"               %% "zio-test-sbt"                      % V.zio               % Test,
      "dev.zio"               %% "zio-test-magnolia"                 % V.zio               % Test,
      "dev.zio"               %% "zio-logging"                       % V.zioLogging,
      "dev.zio"               %% "zio-logging-slf4j"                 % V.zioLogging,
      "io.grpc"                % "grpc-netty"                        % V.grpcNetty,
      "io.getquill"           %% "quill-jdbc-zio"                    % V.quill,
      "org.postgresql"         % "postgresql"                        % V.postgres,
      "io.github.arainko"     %% "ducktape"                          % V.ducktape,
      "io.github.scottweaver" %% "zio-2-0-testcontainers-postgresql" % V.zioTestcontainers % Test,
      "io.github.scottweaver" %% "zio-2-0-db-migration-aspect"       % V.zioTestcontainers % Test,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  )

lazy val protos = (project in file("services/protos"))
  .settings(
    Compile / PB.targets      := Seq(
      scalapb.gen(grpc = true)          -> (Compile / sourceManaged).value,
      scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value,
    ),
    Compile / PB.protoSources := Seq(
      (ThisBuild / baseDirectory).value / "services" / "protos" / "src" / "main" / "protobuf"
    ),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
    ),
  )

lazy val domain = (project in file("services/domain"))
  .settings(stdSettings("domain"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio"     %% "zio-json"              % V.zioJson,
      "dev.zio"     %% "zio-schema"            % V.zioSchema,
      "dev.zio"     %% "zio-schema-derivation" % V.zioSchema,
      "io.getquill" %% "quill-jdbc-zio"        % V.quill,
    )
  )

def flywaySettings = Seq(
  flywayLocations += "db/migration",
  flywayUrl      := "jdbc:postgresql://localhost:15432/test",
  flywayUser     := "postgres",
  flywayPassword := "admin",
)
