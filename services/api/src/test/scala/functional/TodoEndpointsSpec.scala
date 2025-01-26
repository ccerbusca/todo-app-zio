package functional

import api.JwtContent
import api.auth.Auth
import api.endpoints.TodoEndpoints
import api.request.AddTodo
import api.response.TodoResponse
import api.services.*
import db.entities.User
import db.repos.{ TodoRepo, UserRepo }
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.github.arainko.ducktape.*
import utils.LoginUtils
import utils.testinstances.{ AddTodoGenerator, UserRegisterGenerator }
import zio.*
import zio.http.*
import zio.http.Header.Authorization
import zio.json.*
import zio.test.*

object TodoEndpointsSpec extends BaseFunctionalTest {

  override def tests: Spec[db.QuillPostgres, Any] =
    suite("TodoEndpointsSpec")(
      test("POST /todo") {
        val addTodo = AddTodo("title", "content")
        val request = Request.post(
          body = Body.fromString(addTodo.toJson),
          url = url"/todo",
        )
        checkSuccess(request) {
          _ == TodoResponse("title", "content", false).toJson
        }
      },
      test("GET /todos") {
        val request = Request.get(
          url"/todos"
        )
        for {
          user     <- LoginUtils.testUser
          addTodos <- ZIO.collectAll(Set.fill(3)(AddTodoGenerator.generate))
          todos    <- ZIO.foreachPar(addTodos)(addTodo => ZIO.serviceWithZIO[TodoRepo](_.add(addTodo, user.id)))
          result   <- checkSuccessWithUser(user, request) {
            _.fromJson[Set[TodoResponse]].toOption.get == todos.map(_.to[TodoResponse])
          }
        } yield result
      },
      test("POST /todo/complete/{todoId}") {
        for {
          user    <- LoginUtils.testUser
          addTodo <- AddTodoGenerator.generate
          todo    <- ZIO.serviceWithZIO[TodoRepo](_.add(addTodo, user.id))
          result  <- checkSuccessWithUser(
            user,
            Request.post(body = Body.empty, url = url"/todo/complete/${todo.id}"),
          ) {
            _.fromJson[TodoResponse].toOption.get.completed == true
          }
        } yield result
      },
      test("GET /todo/{todoId}") {
        for {
          user    <- LoginUtils.testUser
          addTodo <- AddTodoGenerator.generate
          todo    <- ZIO.serviceWithZIO[TodoRepo](_.add(addTodo, user.id))
          result  <- checkSuccessWithUser(
            user,
            Request.get(url"todo/${todo.id}"),
          ) { body =>
            val todoResponse = body.fromJson[TodoResponse].toOption.get
            todoResponse.title == addTodo.title &&
            todoResponse.content == addTodo.content &&
            !todoResponse.completed
          }
        } yield result
      },
    )
      .provideSome[db.QuillPostgres](
        Auth[JwtContent],
        TodoEndpoints.make,
        TodoService.live,
        JwtService.live,
        TodoRepo.live,
        UserRepo.live,
        UserRegisterGenerator.instance,
        AddTodoGenerator.instance,
      )

  private def checkSuccessWithUser(
      user: User,
      request: Request,
  )(expectedBodyPredicate: String => Boolean) =
    for {
      token     <- LoginUtils.testToken(user)
      endpoints <- ZIO.serviceWith[TodoEndpoints](_.all)
      response  <- endpoints.runZIO(request.addHeader(Authorization.Bearer(token)))
      body      <- response.body.asString
    } yield assertTrue(response.status.isSuccess, expectedBodyPredicate(body))

  private def checkSuccess(request: Request)(expectedBodyPredicate: String => Boolean) =
    for {
      user   <- LoginUtils.testUser
      result <- checkSuccessWithUser(user, request)(expectedBodyPredicate)
    } yield result

}
