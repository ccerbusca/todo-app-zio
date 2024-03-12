package api.endpoints

import api.JwtContent
import api.auth.Auth
import api.errors.ApiError
import api.request.AddTodo
import api.response.TodoResponse
import api.services.{JwtService, TodoService}
import zio.*
import zio.http.*
import zio.http.endpoint.*

case class TodoEndpoints(todoService: TodoService) {

  private val add =
    TodoEndpoints
      .addTodo
      .implement {
        Handler.fromFunctionZIO { addTodo =>
          Auth.authContext[JwtContent].flatMap { ctx =>
            todoService.add(addTodo, ctx.id)
          }
        }
      }

  private val allForUser =
    TodoEndpoints
      .allTodosForUser
      .implement {
        Handler.fromZIO {
          Auth.authContext[JwtContent].flatMap { ctx =>
            todoService.allForUser(ctx.id)
          }
        }
      }

  private val markCompleted =
    TodoEndpoints
      .markCompleted
      .implement {
        Handler.fromFunctionZIO { id =>
          Auth.authContext[JwtContent].flatMap { ctx =>
            todoService.ownedBy(id, ctx.id) *>
              todoService.markCompleted(id)
          }
        }
      }

  private val getTodoById =
    TodoEndpoints
      .getTodoById
      .implement {
        Handler.fromFunctionZIO { id =>
          Auth.authContext[JwtContent].flatMap { ctx =>
            todoService.ownedBy(id, ctx.id) *>
              todoService.get(id)
          }
        }
      }

  private val routes = Routes(
    add,
    allForUser,
    markCompleted,
    getTodoById,
  ) @@ api.auth.authMiddleware

  val all: HttpApp[JwtService & Auth[JwtContent]] = routes.toHttpApp

}

object TodoEndpoints {

  val make: URLayer[TodoService, TodoEndpoints] = ZLayer.fromFunction(TodoEndpoints.apply)

  private val addTodo =
    Endpoint(Method.POST / "todo")
      .in[AddTodo]
      .out[TodoResponse]
      .outError[ApiError](Status.BadRequest)

  private val allTodosForUser =
    Endpoint(Method.GET / "todos")
      .out[List[TodoResponse]]
      .outError[ApiError](Status.Unauthorized)

  private val markCompleted =
    Endpoint(Method.GET / "todo" / "complete" / int("todoId"))
      .out[TodoResponse]
      .outError[ApiError](Status.NotFound)

  private val getTodoById =
    Endpoint(Method.GET / "todo" / int("todoId"))
      .out[TodoResponse]
      .outError[ApiError](Status.NotFound)

}
