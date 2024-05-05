resolvers ++= Resolver.sonatypeOssRepos("releases") ++ Resolver.sonatypeOssRepos("snapshots")

addSbtPlugin("ch.epfl.scala"         % "sbt-scalafix"        % "0.12.1")
addSbtPlugin("io.github.davidmweber" % "flyway-sbt"          % "7.4.0")
addSbtPlugin("com.github.sbt"        % "sbt-native-packager" % "1.9.16")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.7")

libraryDependencies += "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "0.6.1"
