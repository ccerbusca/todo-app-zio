package testinstances

import domain.generators.Generator
import zio.*

case class TestGenerator[T](generator: Generator[T], buffer: Ref[List[T]]) extends Generator[T] {
  override def generate: UIO[T] =
    buffer.getAndUpdate {
      case _ :: tail => tail
      case Nil => Nil
    }.map(_.headOption).some <> generator.generate

  def feedIds(ids: T*): UIO[Unit] =
    buffer.update(_ ++ ids.toList)
}

object TestGenerator {
  def test[T: Tag]: URLayer[Generator[T], TestGenerator[T]] =
    ZLayer.fromZIO {
      for {
        ref <- Ref.make(List.empty[T])
        generator <- ZIO.service[Generator[T]]
      } yield TestGenerator(generator, ref)
    }

  def feedIds[T: Tag](ids: T*): RIO[TestGenerator[T], Unit] =
    ZIO.serviceWithZIO[TestGenerator[T]](_.feedIds(ids: _*))
}
