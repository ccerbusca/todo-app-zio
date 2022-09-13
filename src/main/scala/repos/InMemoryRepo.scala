package repos

import domain.WithId
import domain.errors.ApiError.*
import zio.*
import zio.concurrent.ConcurrentMap

import java.util.UUID

trait InMemoryRepo[T <: WithId[ID], ID] extends Repo[T, ID] {
  def find(pred: T => Boolean): Task[T]
  def filter(pred: T => Boolean): Task[List[T]]
  
  def update(id: ID, updateF: T => T): Task[T]
}

case class InMemoryRepoLive[T <: WithId[ID], ID](concurrentMap: ConcurrentMap[ID, T]) extends InMemoryRepo[T, ID] {
  override def add(entity: T): ZIO[Any, Throwable, T] =
    concurrentMap
      .put(entity.id, entity)
      .as(entity)

  override def get(id: ID): ZIO[Any, Throwable, T] =
    concurrentMap
      .get(id)
      .someOrFail(NotFound)

  override def find(pred: T => Boolean): ZIO[Any, Throwable, T] =
    concurrentMap
      .collectFirst { case (_, v) if pred(v) => v }
      .someOrFail(NotFound)

  override def filter(pred: T => Boolean): Task[List[T]] =
    concurrentMap
      .toList
      .map(_.map(_._2).filter(pred))

  override def update(id: ID, updateF: T => T): Task[T] =
    concurrentMap
      .computeIfPresent(id, (_, v) => updateF(v))
      .someOrFail(NotFound)
}

object InMemoryRepo {
  def live[T <: WithId[ID] : Tag, ID: Tag]: ZLayer[ConcurrentMap[ID, T], Nothing, InMemoryRepoLive[T, ID]] =
    ZLayer {
      ZIO.service[ConcurrentMap[ID, T]].map(InMemoryRepoLive.apply)
    }

  def add[T <: WithId[ID] : Tag, ID: Tag](entity: T): ZIO[InMemoryRepo[T, ID], Throwable, T] =
    ZIO.serviceWithZIO(_.add(entity))

  def find[T <: WithId[ID] : Tag, ID: Tag](pred: T => Boolean): ZIO[InMemoryRepo[T, ID], Throwable, T] =
    ZIO.serviceWithZIO(_.find(pred))
    
  def update[T <: WithId[ID] : Tag, ID: Tag](id: ID, updateF: T => T): ZIO[InMemoryRepo[T, ID], Throwable, T] =
    ZIO.serviceWithZIO(_.update(id, updateF))
}
