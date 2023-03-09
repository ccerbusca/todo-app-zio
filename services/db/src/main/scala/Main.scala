import io.grpc.Status
import repos.*
import scalapb.zio_grpc.*
import services.TodoServiceGrpc
import todos.todo.*
import todos.todo.ZioTodo.TodoService
import zio.*

object Main extends ServerMain {

  override def services: ServiceList[Any] =
    ServiceList
      .addFromEnvironment[TodoServiceGrpc]
      .provide(
        TodoServiceGrpc.make
      )

}
