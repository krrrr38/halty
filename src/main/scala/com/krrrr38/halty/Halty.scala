package com.krrrr38.halty

object Halty extends HaltyParser {

  def apply(input: String): List[Block] = {
    parseAll(body, normalize(input)) match {
      case Success(chunks, _) => collect(chunks)
      case NoSuccess(msg, next) => throw new IllegalStateException(s"BODY: $msg : Line ${next.pos.line}, Column ${next.pos.column}")
    }
  }

  private def normalize(input: String): String =
    if (input.endsWith("\n")) input else input + "\n"

  // merge continuous paragraphs & tablerows
  private def collect(chunks: List[Block]): List[Block] = chunks match {
    case (p1: Paragraph) :: (p2: Paragraph) :: tail => (p1 ++ p2) :: collect(tail)
    case (table: Table) :: (tablerow: TableRow) :: tail => (table ++ tablerow) :: collect(tail)
    case (tablerow: TableRow) :: tail => collect(Table(List(tablerow)) :: tail)
    case head :: tail => head :: collect(tail)
    case Nil => Nil
  }
}

