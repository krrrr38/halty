package com.krrrr38.halty.markdown

import com.krrrr38.halty._
import org.specs2.mutable.Specification

class MarkdownGeneratorTest extends Specification {
  object MarkdownIdentityConfig extends DefaultMarkdownGeneratorConfig {
    override val fetcher: Fetcher = new Fetcher {
      override def fetchTitle(url: String) = url
    }
  }
  val generator = new MarkdownGenerator(MarkdownIdentityConfig)

  "MarkdownGeneratorTest" should {
    "generate" in {
      "header" in {
        generator.generate(List(Header(H3, Inline("text")))) must
          ==/("## text")
        generator.generate(List(Header(H4, Inline("text")))) must
          ==/("### text")
        generator.generate(List(Header(H5, Inline("text")))) must
          ==/("#### text")
      }
      "blockquote" in {
        generator.generate(List(BlockQuote(List(Paragraph(List(Inline("text")))), None))) must
          ==/("> text")
        generator.generate(List(BlockQuote(List(Paragraph(List(Inline("text")))), Some(HttpRawTitle("url", "title"))))) must
          ==/("> text\n> <cite>[title](url)</cite>")
      }
      "ol list" in {
        generator.generate(List(OL(1, List(LI(Inline("text"), None))))) must
          ==/("1. text")
        generator.generate(List(OL(1, List(
          LI(Inline("text1"), Some(OL(2, List(LI(Inline("text2"), None))))),
          LI(Inline("text3"), None))))) must
          ==/("1. text1\n  1. text2\n1. text3")
      }
      "ul list" in {
        generator.generate(List(UL(1, List(LI(Inline("text"), None))))) must
          ==/("- text")
        generator.generate(List(UL(1, List(
          LI(Inline("text1"), Some(UL(2, List(LI(Inline("text2"), None))))),
          LI(Inline("text3"), None))))) must
          ==/("- text1\n  - text2\n- text3")
      }
      "dl" in {
        generator.generate(List(DL(List(DLItem("def1", Inline("detail1")), DLItem("def2", Inline("detail2")))))) must
          ==/("<dl><dt>def1</dt><dd>detail1</dd>\n<dt>def2</dt><dd>detail2</dd></dl>")
      }
      "empty" in {
        generator.generate(List(Empty)) must ==/("\n")
      }
      "paragraph" in {
        generator.generate(List(Paragraph(List(Inline("text"))))) must
          ==/("text")
        generator.generate(List(Paragraph(List(Inline("text"), Inline("text"))))) must
          ==/("text  \ntext")
      }
      "pre" in {
        generator.generate(List(Pre(List(Paragraph(List(Inline("text"))))))) must
          ==/("```\ntext\n```")
      }
      "superpre" in {
        generator.generate(List(SuperPre("text"))) must
          ==/("```\ntext\n```")
        generator.generate(List(SuperPre("text", Some("java")))) must
          ==/("```java\ntext\n```")
      }
      "table" in {
        generator.generate(List(Table(List(
          TableRow(List(TableData(Inline("1-1"), true), TableData(Inline("1-2"), true))),
          TableRow(List(TableData(Inline("2-1"), false), TableData(Inline("2-2"), false)))
        )))) must
          ==/("|**1-1**|**1-2**|\n| ------ | ------ |\n|2-1|2-2|")
      }
    }

  }
}
