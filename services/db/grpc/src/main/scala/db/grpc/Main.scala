package db.grpc

import db.grpc.services.*
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import scalapb.zio_grpc.*
import todos.todo.ZioTodo.TodoService
import zio.*
import zio.Console.printLine
import zio.logging.consoleLogger

object Main extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> consoleLogger()

  private def welcome: ZIO[Any, Throwable, Unit] =
    printLine("Server is running. Press Ctrl-C to stop.")

  private def builder = ServerBuilder
    .forPort(9091)
    .addService(ProtoReflectionService.newInstance())

  private def services: ServiceList[TodoServiceGrpc & UserServiceGrpc] =
    ServiceList
      .addFromEnvironment[TodoServiceGrpc]
      .addFromEnvironment[UserServiceGrpc]

  private def serverLive: ZLayer[TodoServiceGrpc & UserServiceGrpc, Throwable, Server] =
    ServerLayer.fromServiceList(builder, services)

  private def app: ZIO[TodoServiceGrpc & UserServiceGrpc, Throwable, Nothing] = welcome *> serverLive.launch

  def run: ZIO[Any, Throwable, Nothing] = app
    .provide(
      UserServiceGrpc.make,
      TodoServiceGrpc.make,
      db.postgresDefault,
    )

}
