import io.grpc.{ ServerBuilder, Status }
import io.grpc.protobuf.services.ProtoReflectionService
import repos.*
import scalapb.zio_grpc.*
import services.{ TodoServiceGrpc, UserServiceGrpc }
import todos.todo.*
import todos.todo.ZioTodo.TodoService
import zio.*
import zio.Console.printLine
import zio.logging.{ console, LogFormat }

object Main extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> console(LogFormat.default)

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

  def run = app
    .provide(
      UserServiceGrpc.make,
      TodoServiceGrpc.make,
      db.postgresDefault
    )

}
