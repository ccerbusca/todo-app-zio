package services.repos

import domain.WithId
import zio.*

import java.util.UUID

trait Repo[T <: WithId[Id], Id] {
  def get(id: Id): Task[T]
  def find(pred: T => Boolean): Task[T]
}
