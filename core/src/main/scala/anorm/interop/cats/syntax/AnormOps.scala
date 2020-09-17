package anorm.interop.cats.syntax

import anorm._
import cats._
import cats.syntax.all._

trait AnormOps {
  implicit def toSqlResultOps[A](sqlResult: SqlResult[A]): SqlResultOps[A] =
    new SqlResultOps[A](sqlResult)

  implicit def toRowParserOps[A](rowParser: RowParser[A]): RowParserOps[A] =
    new RowParserOps[A](rowParser)
}

class RowParserOps[A](val rowParser: RowParser[A]) extends AnyVal {
  def parseRowIn[F[_] : ApplicativeError[*[_], Throwable]]: Row => F[A] =
    rowParser.apply(_).liftTo[F]
}

class SqlResultOps[A](val sqlResult: SqlResult[A]) extends AnyVal {
  def liftTo[F[_] : ApplicativeError[*[_], Throwable]]: F[A] = sqlResult match {
    case Success(x) => x.pure[F]
    case Error(e) => e.toFailure.liftTo[F]
  }
}
