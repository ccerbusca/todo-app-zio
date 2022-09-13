package functional

import domain.{Todo, User}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import repos.todo.TodoRepo
import repos.user.UserRepo
import services.generators.{IdGenerator, IntGenerator}
import testinstances.*
import zio.*
import zio.test.*

object TodoRepoLiveSpec extends ZIOSpecDefault {
  override def spec =
    (suite("TodoRepoLiveSpec")(

      test("Todo should be correctly fetched") {
        for {
          user <- UserGenerator.generate
          _ <- UserRepo.add(user)
          todo <- TodoGenerator.generate(user)
          inserted <- TodoRepo.add(todo)
        } yield assertTrue(inserted == todo)
      },

      test("Todo should be fetched correctly by id") {
        for {
          user <- UserGenerator.generate
          _ <- UserRepo.add(user)

          todo <- TodoGenerator.generate(user)
          inserted <- TodoRepo.add(todo)
          fetched <- TodoRepo.get(todo.id)
        } yield assertTrue(inserted == fetched)
      },

      test("Fetch all Todos for a user") {
        for {
          u1 <- UserGenerator.generate.flatMap(UserRepo.add)
          u2 <- UserGenerator.generate.flatMap(UserRepo.add)
          todosUser1 <- ZIO.collectAll(Set.fill(3)(TodoGenerator.generate(u1)))
          todosUser2 <- ZIO.collectAll(Set.fill(3)(TodoGenerator.generate(u2)))
          _ <- ZIO.foreachParDiscard(todosUser1 ++ todosUser2)(TodoRepo.add)
          todos <- TodoRepo.findAllByUserId(u1.id) <&> TodoRepo.findAllByUserId(u2.id)
          (t1, t2) = todos
        } yield assertTrue(t1.forall(todosUser1.contains) && t2.forall(todosUser2.contains))
      },

      test("Mark a todo as completed") {
        for {
          user <- UserGenerator.generate.flatMap(UserRepo.add)

          todo <- TodoGenerator.generate(user)
          todo <- TodoRepo.add(todo)
          _ <- TodoRepo.markCompleted(todo.id)
          fetched <- TodoRepo.get(todo.id)
        } yield assertTrue(fetched.completed)
      },

      test("Check if a todo is owned by a user") {
        for {
          u1 <- UserGenerator.generate.flatMap(UserRepo.add)
          u2 <- UserGenerator.generate.flatMap(UserRepo.add)
          todo <- TodoGenerator.generate(u1)
          todo <- TodoRepo.add(todo)
          isU1Owner <- TodoRepo.ownedBy(todo.id, u1.id)
          isU2Owner <- TodoRepo.ownedBy(todo.id, u2.id)
        } yield assertTrue(isU1Owner && !isU2Owner)
      }

    ) @@ DbMigrationAspect.migrate()())
      .provide(
        TodoRepo.live,
        UserRepo.live,
        IdGenerator.int(),
        UserGenerator.instance,
        TodoGenerator.instance,
        ZPostgreSQLContainer.Settings.default,
        ZPostgreSQLContainer.live,
        Quill.Postgres.fromNamingStrategy(SnakeCase),
      )
}
