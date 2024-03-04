//package repos
//import api.request.AddTodo
//import domain.Todo
//import domain.errors.ApiError
//import todos.todo.ZioTodo.TodoServiceClient
//import zio.{IO, Task}
//
//case class TodoRepoGrpc (todoServiceClient: TodoServiceClient) extends TodoRepo {
//  override def findAllByUserId(userId: User.ID): Task[List[Todo]] = ???
//
//  override def markCompleted(id: Todo.ID): Task[Todo] = ???
//
//  override def get(id: ID): IO[ApiError, Todo] = ???
//
//  override def add(entity: AddTodo, userId: User.ID): Task[Todo] = ???
//
//  override def delete(id: ID): Task[