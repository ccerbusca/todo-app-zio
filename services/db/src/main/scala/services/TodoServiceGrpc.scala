package services

import io.github.arainko.ducktape.*
import io.grpc.Status
import repos.TodoRepo
import scalapb.UnknownFieldSet
import todos.todo.*
import todos.todo.ZioTodo.TodoService
import zio.*
import zio.stream.*

extension (t: entities.Todo) {

  def toResponse =
    t.into[Todo]
      .transform(Field.const(_.unknownFields, UnknownFieldSet.empty))

}

case class TodoServiceGrpc(todoRepo: TodoRepo) extends TodoService {

  override def addTodo(request: AddTodoRequest): IO[Status, Todo] =
    todoRepo
      .add(request.entity.get, request.userId)
      .map(_.toResponse)
      .mapError(_ => Status.ALREADY_EXISTS)

  override def getTodo(request: GetTodoRequest): IO[Status, Todo] =
    todoRepo
      .get(request.id)
      .some
      .map(_.toResponse)
      .mapError(_ => Status.NOT_FOUND)

  override def allForUser(request: AllForUserRequest): Stream[Status, Todo] =
    ZStream
      .fromIterableZIO(
        todoRepo
          .findAllByUserId(request.userId)
          .mapError(_ => Status.INTERNAL)
      )
      .map(_.toResponse)

  override def markCompleted(request: MarkCompletedRequest): IO[Status, Todo] =
    todoRepo
      .markCompleted(request.todoId)
      .mapError(_ => Status.INTERNAL)
      .map(_.toResponse)

}

object TodoServiceGrpc {

  val make: TaskLayer[TodoServiceGrpc] = ZLayer.make[TodoServiceGrpc](
    ZLayer.fromFunction(TodoServiceGrpc.apply),
    TodoRepo.live,
    repos.db.postgresDefault,
  )

}
