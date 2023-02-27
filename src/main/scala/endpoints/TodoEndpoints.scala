package endpoints

import auth.Auth
import domain.*
import domain.api.request.*
import domain.api.response.TodoResponse
import domain.errors.ApiError
import services.TodoService
import zio.*
import zio.http.App
import zio.http.codec.HttpCodec.*
import zio.http.endpoint.*
import zio.http.model.Status

case class TodoEndpoints(todoService: TodoService) {

  def add =
    TodoEndpoints
      .addTodo
      .implement { addTodo =>
        Auth.authContext[User].flatMap { user =>
          todoService.add(addTodo, user)
        }
      }

  def allForUser =
    TodoEndpoints
      .allTodosForUser
      .implement { _ =>
        Auth.authContext[User].flatMap { user =>
          todoService.allForUser(user)
        }
      }

  def markCompleted =
    TodoEndpoints
      .markCompleted
      .implement { id =>
        Auth.authContext[User].flatMap { user =>
          todoService.ownedBy(id, user) *> todoService.markCompleted(id)
        }
      }

  def getTodoById =
    TodoEndpoints
      .getTodoById
      .implement { id =>
        Auth.authContext[User].flatMap { user =>
          todoService.ownedBy(id, user) *> todoService.get(id)
        }
      }

  def all = add ++ allForUser ++ markCompleted ++ getTodoById
}

object TodoEndpoints {

  val make: ZIO[TodoService, Nothing, App[Auth[User]]] =
    for {
      todoService <- ZIO.service[TodoService]
      todoEndpoints = TodoEndpoints(todoService)
      routes        = todoEndpoints.all
    } yield routes.toApp

  private val addTodo =
    Endpoint
      .post("todo")
      .in[AddTodo]
      .out[TodoResponse]
      .outError[ApiError](Status.BadRequest)

  private val allTodosForUser =
    Endpoint
      .get("todos")
      .out[List[TodoResponse]]
      .outError[ApiError](Status.Unauthorized)

  private val markCompleted =
    Endpoint
      .post("todo" / "complete" / int("todoId"))
      .out[TodoResponse]
      .outError[ApiError](Status.NotFound)

  private val getTodoById =
    Endpoint
      .get("todo" / int("todoId"))
      .out[TodoResponse]
      .outError[ApiError](Status.NotFound)

}
