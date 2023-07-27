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
        .orElseFail(Status.INVALID_ARGUMENT)
      response <- todoRepo
        .add(entity, request.userId)
        .mapBoth(_ => Status.INTERNAL, _.toResponse)
    } yield response

  override def getTodo(request: Id): IO[Status, Todo] =
    todoRepo
      .get(request.id)
      .some
      .mapBoth(_ => Status.NOT_FOUND, _.toResponse)

  override def allForUser(request: Id): Stream[Status, Todo] =
    ZStream
      .fromIterableZIO(
        todoRepo
          .findAllByUserId(request.id)
          .orElseFail(Status.INTERNAL)
      )
      .map(_.toResponse)

  override def markCompleted(request: Id): IO[Status, Todo] =
    todoRepo
      .markCompleted(request.id)
      .mapBoth(_ => Status.INTERNAL, _.toResponse)

}

object TodoServiceGrpc {

  val make: RLayer[db.QuillPostgres, TodoServiceGrpc] = ZLayer.makeSome[db.QuillPostgres, TodoServiceGrpc](
    ZLayer.fromFunction(TodoServiceGrpc.apply),
    TodoRepo.live,
  )

  extension (t: entities.Todo) {

    def toResponse: Todo =
      t.into[Todo]
        .transform(
          Field.const(_.unknownFields, UnknownFieldSet.empty),
          Field.renamed(_.userId, _.parentId),
        )

  }

}
