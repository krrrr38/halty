package com.krrrr38.halty

sealed trait Chunk
sealed trait Block extends Chunk
sealed trait TemporaryBlock extends Block // only for parsing

sealed trait HeaderTag
case object H3 extends HeaderTag
case object H4 extends HeaderTag
case object H5 extends HeaderTag
case class Header(tag: HeaderTag, inline: Inline) extends Block

case class BlockQuote(blocks: List[Block], http: Option[Http] = None) extends Block

case class DL(items: List[DLItem]) extends Block
case class DLItem(definition: String, detail: Inline) extends Chunk

sealed trait ListTag extends Block
case class OL(level: Int, items: List[LI]) extends ListTag
case class UL(level: Int, items: List[LI]) extends ListTag
case class LI(inline: Inline, nest: Option[ListTag]) extends Chunk

case class SuperPre(content: String, lang: Option[String] = None) extends Block

case class Pre(blocks: List[Block]) extends Block

case class Paragraph(inlines: List[Inline]) extends Block {
  def ++(other: Paragraph): Paragraph = Paragraph(inlines ++ other.inlines)
}

case object Empty extends Block

case class Inline(text: String) extends Chunk

sealed trait Http extends Chunk {
  val url: String
}
case class HttpRawTitle(url: String, title: String) extends Http
case class HttpAutoTitle(url: String) extends Http

case class Table(rows: List[TableRow]) extends Block {
  def ++(tablerow: TableRow): Table = Table(rows ++ List(tablerow))
}
case class TableRow(cells: List[TableData]) extends TemporaryBlock
case class TableData(inline: Inline, isEmphasis: Boolean = false) extends Chunk
