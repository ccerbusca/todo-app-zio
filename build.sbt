ThisBuild / scalaVersion     := "3.1.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "example"
ThisBuild / organizationName := "example"

val zioVersion = "2.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "ZIOTest",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"               % zioVersion,
      "dev.zio" %% "zio-streams"       % zioVersion,
      "dev.zio" %% "zio-test"          % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt"      % zioVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test,
      "dev.zio" %% "zio-json"          % "0.3.0-RC10",
      "io.d11"  %% "zhttp"             % "2.0.0-RC10",
      "io.d11"  %% "zhttp-test"        % "2.0.0-RC9" % Test,

    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalacOptions += "-no-indent"
  )
