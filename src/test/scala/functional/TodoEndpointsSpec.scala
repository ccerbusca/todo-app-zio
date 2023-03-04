package functional

import auth.Auth
import domain.User
import domain.api.JwtContent
import domain.api.request.AddTodo
import domain.api.response.TodoResponse
import endpoints.TodoEndpoints
import io.getquill.jdbczio.Quill
import io.github.arainko.ducktape.*
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import repos.*
import services.*
import utils.LoginUtils
import utils.testinstances.{AddTodoGenerator, UserRegisterGenerator}
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*

object TodoEndpointsSpec extends ZIOSpecDefault {

  override def spec =
    (suite("TodoEndpointsSpec")(
      test("POST /todo") {
        val addTodo = AddTodo("title", "content")
        val request = Request.post(
          Body.fromString(addTodo.toJson),
          URL.fromString("/todo").toOption.get,
        )
        checkSuccess(request, _ == TodoResponse("title", "content", false).toJson)
      },
      test("GET /todos") {
        val request = Request.get(
          URL.fromString("/todos").toOption.get
        )
        for {
          user     <- LoginUtils.testUser
          addTodos <- ZIO.collectAll(Set.fill(3)(AddTodoGenerator.generate))
          todos    <- ZIO.foreachPar(addTodos)(addTodo => ZIO.serviceWithZIO[TodoRepo](_.add(addTodo, user.id)))
          result   <- checkSuccessWithUser(
            user,
            request,
            _.fromJson[Set[TodoResponse]].toOption.get == todos.map(_.to[TodoResponse]),
          )
        } yield result
      },
      test("POST /todo/complete/{todoId}") {
        for {
          user    <- LoginUtils.testUser
          addTodo <- AddTodoGenerator.generate
          todo    <- ZIO.serviceWithZIO[TodoRepo](_.add(addTodo, user.id))
          result  <- checkSuccessWithUser(
            user,
            Request.post(Body.empty, URL.fromString(s"/todo/complete/${todo.id}").toOption.get),
            _.fromJson[TodoResponse].toOption.get.completed == true,
          )
        } yield result
      },
      test("GET /todo/{todoId}") {
        for {
          user    <- LoginUtils.testUser
          addTodo <- AddTodoGenerator.generate
          todo    <- ZIO.serviceWithZIO[TodoRepo](_.add(addTodo, user.id))
          result  <- checkSuccessWithUser(
            user,
            Request.get(URL.fromString(s"todo/${todo.id}").toOption.get),
            body => {
              val todoResponse = body.fromJson[TodoResponse].toOption.get
              todoResponse.title == addTodo.title &&
              todoResponse.content == addTodo.content &&
              !todoResponse.completed
            },
          )
        } yield result
      },
    ) @@ DbMigrationAspect.migrate()())
      .provide(
        TodoEndpoints.make,
        TodoService.live,
        JwtService.live,
        TodoRepo.live,
        UserRepo.live,
        UserRegisterGenerator.instance,
        AddTodoGenerator.instance,
        ZPostgreSQLContainer.Settings.default,
        ZPostgreSQLContainer.live,
        Quill.Postgres.fromNamingStrategy(io.getquill.SnakeCase),
      )

  def checkSuccessWithUser(
      user: User,
      request: Request,
      expectedBodyPredicate: String => Boolean,
  ) =
    for {
      token     <- LoginUtils.testToken(user)
      endpoints <- ZIO.serviceWith[TodoEndpoints](_.all)
      response  <- endpoints.runZIO(request.withAuthorization(s"Bearer $token"))
      body      <- response.body.asString
    } yield assertTrue(response.status.isSuccess) && assertTrue(expectedBodyPredicate(body))

  def checkSuccess(request: Request, expectedBodyPredicate: String => Boolean) =
    for {
      user   <- LoginUtils.testUser
      result <- checkSuccessWithUser(user, request, expectedBodyPredicate)
    } yield result

}
