package api.clients

import io.grpc.ManagedChannelBuilder
import scalapb.zio_grpc.ZManagedChannel
import todos.todo.ZioTodo.TodoServiceClient
import users.user.ZioUser.UserServiceClient
import zio.*

object Grpc {

  val userServiceClient: TaskLayer[UserServiceClient] =
    UserServiceClient.live(
      ZManagedChannel(
        ManagedChannelBuilder.forAddress("localhost", 9091).usePlaintext()
      )
    )

  val todoServiceClient: TaskLayer[TodoServiceClient] =
    TodoServiceClient.live(
      ZManagedChannel(
        ManagedChannelBuilder.forAddress("localhost", 9091).usePlaintext()
      )
    )

}
