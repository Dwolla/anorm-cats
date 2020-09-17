package anorm.interop.cats.instances

import anorm._
import cats._

object RowParserInstances extends RowParserInstances

trait RowParserInstances {
  implicit val rowParserMonad: Monad[RowParser] = new Monad[RowParser] {
    override def pure[A](x: A): RowParser[A] =
      RowParser.successful.map(_ => x)

    override def flatMap[A, B](fa: RowParser[A])(f: A => RowParser[B]): RowParser[B] =
      fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => RowParser[Either[A, B]]): RowParser[B] = {
      val fa = f(a)

      fa.flatMap {
        case Left(l) => tailRecM(l)(f)
        case Right(r) => fa.map(_ => r)
      }
    }
  }
}
