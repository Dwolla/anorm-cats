# Anorm Cats

Cats typeclass instances for Anorm types.

 * `MonadError[SqlResult, Throwable]`
 * `Traverse[SqlResult]`
 * `Monoid[SqlResult[A]]` for any `A : Monoid`
 * `Monad[RowParser]`

## Usage

`import anorm.interop.cats.implicits._` to bring the instances and the syntax enhancements into scope. (More specific imports are available in the `anorm.interop.cats.syntax` and `anorm.interop.cats.instances` packages.)

### Example

```scala
import anorm._
import anorm.interop.cats.implicits._
import cats._

def parseMyNumberFromRow[F[_] : ApplicativeError[*[_], Throwable]](row: Row): F[Int] =
  SqlParser.get[Int]("my_number")       // RowParser[Int]
    .apply(row)                         // SqlResult[Int]
    .liftTo[F]                          // F[Int]
```
