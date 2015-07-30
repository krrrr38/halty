package com.krrrr38.halty

import scala.util.parsing.combinator.RegexParsers

trait HaltyParser extends RegexParsers {
  override val skipWhitespace = false

  // body       ::= {block}*
  def body: Parser[List[Block]] = block.* <~ EOF.?

  //  block      ::= (h5 | h4 | h3 | blockquote | dl | list
  //                          | superpre | pre | table | p) {space}* "\n"
  def block: Parser[Block] =
    (header | blockQuote | dl | list(1) | superPre | pre | tableRow | p | empty) <~ "[ 　\t]*".r ~ newLine

  //  h3 ::= "\n*" inline
  //  h4 ::= "\n**" inline
  //  h5 ::= "\n***" inline
  private[halty] def header: Parser[Header] = """\*{1,3}""".r ~ inline ^^ {
    case prefix ~ inline => prefix.length match {
      case 1 => Header(H3, inline)
      case 2 => Header(H4, inline)
      case 3 => Header(H5, inline)
      case n => throw new IllegalArgumentException(s"The number of header prefix is invalid: $n")
    }
  }

  // blockquote ::= ">" "httpinline"? ">\n" {block}+ "<<" ignore
  private[halty] def blockQuote: Parser[BlockQuote] = ">" ~> http.? ~ ">\n" ~ """([\S\s]*?)(?=\n<<)""".r <~ "\n<<" ~ "[^\\n]*".r ^^ {
    case maybeHttp ~ _ ~ blocktext => parseAll(block.*, blocktext + "\n") match {
      case Success(blocks, _) => BlockQuote(blocks, maybeHttp)
      case NoSuccess(msg, next) => throw new IllegalStateException(s"BLOCKQUOTE: $msg : Line ${next.pos.line}, Column ${next.pos.column}")
    }
  }

  // dl ::= {dlitem "\n"}+
  private[halty] def dl: Parser[DL] = rep1sep(dlitem, "\n") ^^ DL

  // ":" dlinline ":" inline
  // dlinline ::= """[^\n:]+""".r
  private[halty] def dlitem: Parser[DLItem] = ":" ~> """[^\n:]+""".r ~ ":" ~ inline ^^ {
    case definition ~ _ ~ detail => DLItem(definition, detail)
  }

  // list ::= {ulitem}+ | {olitem}+
  private[halty] def list(level: Int): Parser[ListTag] = rep1sep(ulitem(level), "\n") ^^ {
    case items => UL(level, items)
  } | rep1sep(olitem(level), "\n") ^^ {
    case items => OL(level, items)
  }

  // ulitem ::= "-"+ inline? {"\n" list}?
  private[halty] def ulitem(level: Int): Parser[LI] = repN(level, "-") ~ inline.? ~ ("\n" ~> list(level + 1)).? ^^ {
    case _ ~ inline ~ nestList => LI(inline.getOrElse(Inline("")), nestList)
  }

  // olitem ::= "+"+ inline? {"\n" list}?
  private[halty] def olitem(level: Int): Parser[LI] = repN(level, "+") ~ inline.? ~ ("\n" ~> list(level + 1)).? ^^ {
    case _ ~ inline ~ nestList => LI(inline.getOrElse(Inline("")), nestList)
  }

  // superpre ::= ">|" "[^\n\|]+".r? "|\n" "([\s\S]*?)(?=\n\|\|<)".r "||<" ignore
  private[halty] def superPre: Parser[SuperPre] = ">|" ~> """[^\n\|]+""".r.? ~ "|\n" ~ """([\s\S]*?)(?=\n\|\|<)""".r <~ "\n||<" ~ "[^\\n]*".r ^^ {
    case maybeLang ~ _ ~ content => SuperPre(content, maybeLang)
  }

  // pre ::= ">|\n" {block}+ "|<" ignore
  private[halty] def pre: Parser[Pre] = ">|\n" ~> """([\S\s]*?)(?=\n\|<)""".r <~ "\n|<" ~ "[^\\n]*".r ^^ {
    case blocktext => parseAll(block.*, blocktext + "\n") match {
      case Success(blocks, _) => Pre(blocks)
      case NoSuccess(msg, next) => throw new IllegalStateException(s"PRE: $msg : Line ${next.pos.line}, Column ${next.pos.column}")
    }
  }

  // table ::= {tablerow}+ (continuous tablerow would be merged as table)
  // tablerow ::= "|" {tabledata "|"}+
  // tabledata ::= "*"? """[^\n\|]+""".r
  private[halty] def tableRow: Parser[TableRow] = """\|(.*)(?=\|\n)""".r <~ "|" ^^ {
    case text => TableRow(text.tail.split("\\|").toList.map {
      case text if text.startsWith("*") => TableData(Inline(text.tail), true)
      case text => TableData(Inline(text))
    })
  }

  // http ::= "["? """https?:\/\/[A-Za-z0-9~\/._\?\&=\-%#\+\;,\@\']+""".r ":title=([^\]]+)".r.? "]"?
  private[halty] def http: Parser[Http] = rawHttp ^^ {
    case url => HttpRawTitle(url, url)
  } | brackerHttp ^^ {
    case url ~ None ~ _ ~ None => HttpRawTitle(url, url)
    case url ~ Some(_) ~ _ ~ None => HttpAutoTitle(url)
    case url ~ Some(_) ~ _ ~ Some(rawTitle) => HttpRawTitle(url, rawTitle)
    case url ~ None ~ _ ~ _ => HttpRawTitle(url, url)
  }

  lazy val httpRegex = """https?:\/\/[A-Za-z0-9~\/._\?\&=\-%#\+\;,\@\']+""".r

  private[halty] def rawHttp: Parser[String] = httpRegex

  private[halty] def brackerHttp: Parser[String ~ Option[String] ~ Option[String] ~ Option[String]] = "[" ~> httpRegex ~ ":title".? ~ "=".? ~ "([^]]+)".r.? <~ "]"

  // p ::= {inline}+ (continuous paragraph should be merged)
  private[halty] def p: Parser[Paragraph] = rep1(inline) ^^ Paragraph

  // inline ::= """[^\n]+""".r
  private[halty] def inline: Parser[Inline] = """[^\n]+""".r ^^ Inline

  private[halty] def empty: Parser[Block] = "" ^^ {
    case _ => Empty
  }

  private[halty] def newLine: Parser[String] = "\r".? ~> "\n"

  private[halty] def EOF: util.matching.Regex = "\\z".r
}
