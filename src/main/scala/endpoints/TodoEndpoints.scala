package endpoints

import auth.{Auth, authMiddleware, secureRoutes}
import domain.*
import domain.api.JwtContent
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

  val add =
    TodoEndpoints
      .addTodo
      .implement { addTodo =>
        Auth.authContext[JwtContent].flatMap { jwtContent =>
          todoService.add(addTodo, jwtContent.id)
        }
      }

  val allForUser =
    TodoEndpoints
      .allTodosForUser
      .implement { _ =>
        Auth.authContext[JwtContent].flatMap { jwtContent =>
          todoService.allForUser(jwtContent.id)
        }
      }

  val markCompleted =
    TodoEndpoints
      .markCompleted
      .implement { id =>
        Auth.authContext[JwtContent].flatMap { jwtContent =>
          todoService.ownedBy(id, jwtContent.id) *> todoService.markCompleted(id)
        }
      }

  val getTodoById =
    TodoEndpoints
      .getTodoById
      .implement { id =>
        Auth.authContext[JwtContent].flatMap { jwtContent =>
          todoService.ownedBy(id, jwtContent.id) *> todoService.get(id)
        }
      }

  val all = secureRoutes((add ++ allForUser ++ markCompleted ++ getTodoById).toApp)

}

object TodoEndpoints {

  val make = ZLayer.fromFunction(TodoEndpoints.apply)

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
