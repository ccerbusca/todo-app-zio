package domain

case class Todo(
    id: Todo.ID,
    parentId: User.ID,
    title: String,
    content: String,
    completed: Boolean = false,
) extends WithId[Todo.ID]

object Todo {
  type ID = Long
}
