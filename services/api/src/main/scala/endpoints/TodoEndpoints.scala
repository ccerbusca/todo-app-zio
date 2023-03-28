package endpoints

import api.JwtContent
import api.request.AddTodo
import api.response.TodoResponse
import auth.{Auth, authMiddleware, secureRoutes}
import domain.errors.ApiError
import services.{JwtService, TodoService}
import zio.*
import zio.http.codec.HttpCodec.*
import zio.http.endpoint.*
import zio.http.model.Status
import zio.http.{int as _, *}

case class TodoEndpoints(todoService: TodoService) {

  val add =
    TodoEndpoints
      .addTodo
      .implement { addTodo =>
        ZIO.serviceWithZIO[JwtContent] { jwtContent =>
          todoService.add(addTodo, jwtContent.id)
        }
      }

  val allForUser =
    TodoEndpoints
      .allTodosForUser
      .implement { _ =>
        ZIO.serviceWithZIO[JwtContent] { jwtContent =>
          todoService.allForUser(jwtContent.id)
        }
      }

  val markCompleted =
    TodoEndpoints
      .markCompleted
      .implement { id =>
        ZIO.serviceWithZIO[JwtContent] { jwtContent =>
          todoService.ownedBy(id, jwtContent.id) *> todoService.markCompleted(id)
        }
      }

  val getTodoById =
    TodoEndpoints
      .getTodoById
      .implement { id =>
        ZIO.serviceWithZIO[JwtContent] { jwtContent =>
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
