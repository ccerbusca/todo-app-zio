package db.grpc.services

import api.request.AddTodo
import db.entities
import db.repos.TodoRepo
import io.github.arainko.ducktape.*
import io.grpc.{Status, StatusException}
import scalapb.UnknownFieldSet
import todos.todo.*
import todos.todo.ZioTodo.TodoService
import zio.*
import zio.stream.*

case class TodoServiceGrpc(todoRepo: TodoRepo) extends TodoService {
  import TodoServiceGrpc.*

  override def addTodo(request: AddTodoRequest): IO[StatusException, Todo] =
    for {
      entity   <- ZIO
        .fromOption(request.entity)
        .orElseFail(StatusException(Status.INVALID_ARGUMENT))
      response <- todoRepo
        .add(entity.to[AddTodo], request.userId)
        .mapBoth(_ => StatusException(Status.INTERNAL), _.toResponse)
    } yield response

  override def getTodo(request: Id): IO[StatusException, Todo] =
    todoRepo
      .get(request.id)
      .mapBoth(_ => StatusException(Status.NOT_FOUND), _.toResponse)

  override def allForUser(request: Id): Stream[StatusException, Todo] =
    ZStream
      .fromIterableZIO(
        todoRepo
          .findAllByUserId(request.id)
          .orElseFail(StatusException(Status.INTERNAL))
      )
      .map(_.toResponse)

  override def markCompleted(request: Id): IO[StatusException, Todo] =
    todoRepo
      .markCompleted(request.id)
      .mapBoth(_ => StatusException(Status.INTERNAL), _.toResponse)

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
