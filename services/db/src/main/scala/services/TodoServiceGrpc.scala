package services

import io.github.arainko.ducktape.*
import io.grpc.Status
import repos.{TodoRepo, db}
import scalapb.UnknownFieldSet
import todos.todo.*
import todos.todo.ZioTodo.TodoService
import zio.*
import zio.stream.*

case class TodoServiceGrpc(todoRepo: TodoRepo) extends TodoService {
  import TodoServiceGrpc.*

  override def addTodo(request: AddTodoRequest): IO[Status, Todo] =
    for {
      entity   <- ZIO
        .from(request.entity)
        .mapError(_ => Status.INVALID_ARGUMENT)
      response <- todoRepo
        .add(entity, request.userId)
        .map(_.toResponse)
        .mapError(_ => Status.INTERNAL)
    } yield response

  override def getTodo(request: Id): IO[Status, Todo] =
    todoRepo
      .get(request.id)
      .some
      .map(_.toResponse)
      .mapError(_ => Status.NOT_FOUND)

  override def allForUser(request: Id): Stream[Status, Todo] =
    ZStream
      .fromIterableZIO(
        todoRepo
          .findAllByUserId(request.id)
          .mapError(_ => Status.INTERNAL)
      )
      .map(_.toResponse)

  override def markCompleted(request: Id): IO[Status, Todo] =
    todoRepo
      .markCompleted(request.id)
      .mapError(_ => Status.INTERNAL)
      .map(_.toResponse)

}

object TodoServiceGrpc {

  val make: RLayer[db.QuillPostgres, TodoServiceGrpc] = ZLayer.makeSome[db.QuillPostgres, TodoServiceGrpc](
    ZLayer.fromFunction(TodoServiceGrpc.apply),
    TodoRepo.live,
  )

  extension (t: entities.Todo) {

    def toResponse =
      t.into[Todo]
        .transform(
          Field.const(_.unknownFields, UnknownFieldSet.empty),
          Field.renamed(_.userId, _.parentId),
        )

  }

}
