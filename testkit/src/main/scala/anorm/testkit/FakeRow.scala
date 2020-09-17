package anorm
package testkit

case class FakeRow(data: List[Any], metaDataItems: Seq[MetaDataItem]) extends Row {
  override private[anorm] def metaData = MetaData(metaDataItems)
}
