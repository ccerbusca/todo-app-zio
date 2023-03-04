package functional

import domain.generators.{Generator, IntGenerator}
import domain.{Todo, User}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import repos.{TodoRepo, UserRepo}
import utils.testinstances.{AddTodoGenerator, UserRegisterGenerator}
import zio.*
import zio.test.*

object TodoRepoLiveSpec extends ZIOSpecDefault {

  override def spec =
    (suite("TodoRepoLiveSpec")(
      test("Todo should be correctly inserted and fetched") {
        for {
          userRegister <- UserRegisterGenerator.generate
          user         <- UserRepo.add(userRegister)
          todo         <- AddTodoGenerator.generate
          inserted     <- TodoRepo.add(todo, user.id)
          fetched      <- TodoRepo.get(inserted.id)
        } yield assertTrue(inserted == fetched)
      },
      test("Fetch all Todos for a user") {
        for {
          u1         <- UserRegisterGenerator.generate.flatMap(UserRepo.add)
          u2         <- UserRegisterGenerator.generate.flatMap(UserRepo.add)
          todosUser1 <- ZIO.collectAll(Set.fill(3)(AddTodoGenerator.generate))
          todosUser2 <- ZIO.collectAll(Set.fill(3)(AddTodoGenerator.generate))
          _          <- ZIO.foreachParDiscard(todosUser1)(TodoRepo.add(_, u1.id)) <&>
            ZIO.foreachParDiscard(todosUser2)(TodoRepo.add(_, u2.id))
          todos      <- TodoRepo.findAllByUserId(u1.id) <&> TodoRepo.findAllByUserId(u2.id)
          (t1, t2) = todos
        } yield assertTrue(
          t1.forall(todo => todosUser1.exists(todo2 => todo2.title == todo.title && todo2.content == todo.content)) &&
          t2.forall(todo => todosUser2.exists(todo2 => todo2.title == todo.title && todo2.content == todo.content))
        )
      },
      test("Mark a todo as completed") {
        for {
          user    <- UserRegisterGenerator.generate.flatMap(UserRepo.add)
          addTodo <- AddTodoGenerator.generate
          todo    <- TodoRepo.add(addTodo, user.id)
          _       <- TodoRepo.markCompleted(todo.id)
          fetched <- TodoRepo.get(todo.id)
        } yield assertTrue(fetched.completed)
      },
      test("Check if a todo is owned by a user") {
        for {
          u1        <- UserRegisterGenerator.generate.flatMap(UserRepo.add)
          u2        <- UserRegisterGenerator.generate.flatMap(UserRepo.add)
          todo      <- AddTodoGenerator.generate
          todo      <- TodoRepo.add(todo, u1.id)
          isU1Owner <- TodoRepo.ownedBy(todo.id, u1.id)
          isU2Owner <- TodoRepo.ownedBy(todo.id, u2.id)
        } yield assertTrue(isU1Owner && !isU2Owner)
      },
    ) @@ DbMigrationAspect.migrate()())
      .provide(
        TodoRepo.live,
        UserRepo.live,
        UserRegisterGenerator.instance,
        AddTodoGenerator.instance,
        ZPostgreSQLContainer.Settings.default,
        ZPostgreSQLContainer.live,
        Quill.Postgres.fromNamingStrategy(SnakeCase),
      )

}
