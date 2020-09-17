package anorm.interop.cats.instances

import anorm._
import cats._
import cats.syntax.all._

import scala.annotation.tailrec

object SqlResultInstances extends SqlResultInstances

trait SqlResultInstances {
  implicit val sqlResultMonadErrorTraverse: MonadError[SqlResult, Throwable] with Traverse[SqlResult] =
    new MonadError[SqlResult, Throwable] with Traverse[SqlResult] {
      override def pure[A](x: A): SqlResult[A] = Success(x)

      override def ap[A, B](ff: SqlResult[A => B])(fa: SqlResult[A]): SqlResult[B] =
        ff.flatMap(f => fa.map(f))

      override def traverse[G[_] : Applicative, A, B](fa: SqlResult[A])
                                                     (f: A => G[B]): G[SqlResult[B]] =
        fa.fold(Error(_).pure[G].widen, f(_).map(Success(_)))

      override def foldLeft[A, B](fa: SqlResult[A], b: B)
                                 (f: (B, A) => B): B =
        fa.fold(_ => b, f(b, _))

      override def foldRight[A, B](fa: SqlResult[A], lb: Eval[B])
                                  (f: (A, Eval[B]) => Eval[B]): Eval[B] =
        fa.fold(_ => lb, f(_, lb))

      override def flatMap[A, B](fa: SqlResult[A])(f: A => SqlResult[B]): SqlResult[B] =
        fa.flatMap(f)

      @tailrec
      override def tailRecM[A, B](a: A)(f: A => SqlResult[Either[A, B]]): SqlResult[B] =
        f(a) match {
          case Success(Left(aa)) => tailRecM(aa)(f)
          case Success(Right(b)) => Success(b)
          case e @ Error(_) => e
        }

      override def raiseError[A](e: Throwable): SqlResult[A] =
        Error(SqlRequestError(e))

      override def handleErrorWith[A](fa: SqlResult[A])
                                     (f: Throwable => SqlResult[A]): SqlResult[A] =
        fa match {
          case Success(_) => fa
          case Error(msg) =>
            f(msg.toFailure.exception)
        }
    }

  implicit def sqlResultMonoid[A : Monoid]: Monoid[SqlResult[A]] = new Monoid[SqlResult[A]] {
    override def empty: SqlResult[A] =
      Success(Monoid[A].empty)

    override def combine(x: SqlResult[A], y: SqlResult[A]): SqlResult[A] =
      (x, y).mapN(_ |+| _)
  }
}
