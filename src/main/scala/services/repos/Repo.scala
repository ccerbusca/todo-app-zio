package services.repos

import domain.WithId
import zio.*

import java.util.UUID

trait Repo[T <: WithId[Id], Id] {
  def get(uuid: Id): ZIO[Any, Throwable, T]
}
