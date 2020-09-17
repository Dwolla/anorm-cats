package anorm.interop.cats.instances

import anorm._
import anorm.testkit._
import cats.implicits._
import cats.kernel.Eq
import cats.laws.discipline.{arbitrary => _, _}
import cats.kernel.laws.discipline._
import org.scalacheck.Arbitrary._
import org.scalacheck._
import org.specs2.mutable.Specification
import org.typelevel.discipline.specs2.mutable.Discipline

class AnormInstancesLawsSpec extends Specification with Discipline {

  implicit def arbRowParser[A: Arbitrary]: Arbitrary[RowParser[A]] = Arbitrary {
    for {
      a <- arbitrary[A]
    } yield RowParser.successful.map(_ => a)
  }

  implicit def arbSqlResult[A: Arbitrary]: Arbitrary[SqlResult[A]] = Arbitrary {
    for {
      a <- arbitrary[A]
    } yield Success(a)
  }

  implicit def eqRowParser[A : Eq]: Eq[RowParser[A]] = (x: RowParser[A], y: RowParser[A]) => {
    val row = FakeRow(List.empty, Seq.empty)
    x(row) eqv y(row)
  }

  implicit def eqSqlResult[A : Eq]: Eq[SqlResult[A]] = (x: SqlResult[A], y: SqlResult[A]) => (x, y) match {
    case (Success(xx), Success(yy)) => xx eqv yy
    case (Error(xx), Error(yy)) => xx eqv yy
    case _ => false
  }

  implicit def arbSqlRequestError: Arbitrary[SqlRequestError] = Arbitrary {
    Gen.oneOf(
      arbitrary[Throwable].map(SqlRequestError(_)),
      arbitrary[ColumnNotFound],
      arbitrary[UnexpectedNullableFound],
      arbitrary[SqlMappingError],
      arbitrary[TypeDoesNotMatch],
    )
  }

  implicit def arbColumnNotFound: Arbitrary[ColumnNotFound] = Arbitrary {
    for {
      column <- Gen.asciiPrintableStr
      available <- Gen.listOf(Gen.asciiPrintableStr)
    } yield ColumnNotFound(column, available = available)
  }

  implicit def arbUnexpectedNullableFound: Arbitrary[UnexpectedNullableFound] = Arbitrary {
    for {
      reason <- arbitrary[String]
    } yield UnexpectedNullableFound(reason)
  }

  implicit def arbSqlMappingError: Arbitrary[SqlMappingError] = Arbitrary {
    for {
      reason <- arbitrary[String]
    } yield SqlMappingError(reason)
  }

  implicit def arbTypeDoesNotMatch: Arbitrary[TypeDoesNotMatch] = Arbitrary {
    for {
      reason <- arbitrary[String]
    } yield TypeDoesNotMatch(reason)
  }

  implicit val eqSqlRequestError: Eq[SqlRequestError] = (x: SqlRequestError, y: SqlRequestError) => (x, y) match {
    case (ColumnNotFound(_, _), ColumnNotFound(_, _)) |
         (UnexpectedNullableFound(_), UnexpectedNullableFound(_)) |
         (SqlMappingError(_), SqlMappingError(_)) |
         (TypeDoesNotMatch(_), TypeDoesNotMatch(_)) => x == y
    case _ =>
      x.toFailure.exception == y.toFailure.exception
  }

  implicit val eqThrowable: Eq[Throwable] = Eq.fromUniversalEquals

  "RowParser" in {
    checkAll("Monad[RowParser]", MonadTests[RowParser].stackUnsafeMonad[Int, Int, Int])
  }

  "SqlResult" in {
    checkAll("MonadError[SqlResult, Throwable]", MonadErrorTests[SqlResult, Throwable].monadError[Int, Int, Int])
    checkAll("Traverse[SqlResult]", TraverseTests[SqlResult].traverse[Int, Int, Int, Int, Option, Option])
    checkAll("Monoid[SqlResult[Int]", MonoidTests[SqlResult[Int]].monoid)
    checkAll("Monoid[SqlResult[List[String]]", MonoidTests[SqlResult[List[String]]].monoid)
  }

}
