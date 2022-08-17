package testinstances

import services.generators.IdGenerator
import zio.*

case class TestIdGenerator[T](generator: IdGenerator[T], buffer: Ref[List[T]]) extends IdGenerator[T] {
  override def generate: UIO[T] =
    buffer.getAndUpdate {
      case _ :: tail => tail
      case Nil => Nil
    }.map(_.headOption).some <> generator.generate

  def feedIds(ids: T*): UIO[Unit] =
    buffer.update(_ ++ ids.toList)
}

object TestIdGenerator {
  def test[T: Tag]: URLayer[IdGenerator[T], TestIdGenerator[T]] =
    ZLayer.fromZIO {
      for {
        ref <- Ref.make(List.empty[T])
        generator <- ZIO.service[IdGenerator[T]]
      } yield TestIdGenerator(generator, ref)
    }

  def feedIds[T: Tag](ids: T*): RIO[TestIdGenerator[T], Unit] =
    ZIO.serviceWithZIO[TestIdGenerator[T]](_.feedIds(ids: _*))
}
