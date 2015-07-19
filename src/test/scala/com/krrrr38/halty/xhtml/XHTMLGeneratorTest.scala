package com.krrrr38.halty.xhtml

import com.krrrr38.halty._
import org.specs2.mutable.Specification

class XHTMLGeneratorTest extends Specification {
  object XHTMLIdentityConfig extends DefaultXHTMLGeneratorConfig {
    override val fetcher: Fetcher = new Fetcher {
      override def fetchTitle(url: String) = url
    }
  }
  val generator = new XHTMLGenerator(XHTMLIdentityConfig)

  "generate" should {
    "header" in {
      generator.generate(List(Header(H3, Inline("text")))) must
        ==/("<h3>text</h3>")
      generator.generate(List(Header(H4, Inline("text")))) must
        ==/("<h4>text</h4>")
      generator.generate(List(Header(H5, Inline("text")))) must
        ==/("<h5>text</h5>")
    }
    "blockquote" in {
      generator.generate(List(BlockQuote(List(Paragraph(List(Inline("text")))), None))) must
        ==/("<blockquote><p>text</p></blockquote>")
      generator.generate(List(BlockQuote(List(Paragraph(List(Inline("text")))), Some(HttpRawTitle("url", "title"))))) must
        ==/("""<blockquote cite="url"><p>text</p><cite><a href="url">title</a></cite></blockquote>""")
    }
    "ol list" in {
      generator.generate(List(OL(1, List(LI(Inline("text"), None))))) must
        ==/("<ol><li>text</li></ol>")
      generator.generate(List(OL(1, List(
        LI(Inline("text1"), Some(OL(2, List(LI(Inline("text2"), None))))),
        LI(Inline("text3"), None))))) must
        ==/("<ol><li>text1<ol><li>text2</li></ol></li><li>text3</li></ol>")
    }
    "ul list" in {
      generator.generate(List(UL(1, List(LI(Inline("text"), None))))) must
        ==/("<ul><li>text</li></ul>")
      generator.generate(List(UL(1, List(
        LI(Inline("text1"), Some(UL(2, List(LI(Inline("text2"), None))))),
        LI(Inline("text3"), None))))) must
        ==/("<ul><li>text1<ul><li>text2</li></ul></li><li>text3</li></ul>")
    }
    "dl" in {
      generator.generate(List(DL(List(DLItem("def1", Inline("detail1")), DLItem("def2", Inline("detail2")))))) must
        ==/("<dl><dt>def1</dt><dd>detail1</dd><dt>def2</dt><dd>detail2</dd></dl>")
    }
    "empty" in {
      generator.generate(List(Empty)) must ==/("<br/>")
    }
    "paragraph" in {
      generator.generate(List(Paragraph(List(Inline("text"))))) must
        ==/("<p>text</p>")
    }
    "pre" in {
      generator.generate(List(Pre(List(Paragraph(List(Inline("text"))))))) must
        ==/("<pre><p>text</p></pre>")
    }
    "superpre" in {
      generator.generate(List(SuperPre("text"))) must
        ==/("<pre><code class=\"code\">text</code></pre>")
      generator.generate(List(SuperPre("text", Some("java")))) must
        ==/("<pre><code class=\"code language-java\">text</code></pre>")
    }
    "table" in {
      generator.generate(List(Table(List(
        TableRow(List(TableData(Inline("1-1"), true), TableData(Inline("1-2"), true))),
        TableRow(List(TableData(Inline("2-1"), false), TableData(Inline("2-2"), false)))
      )))) must
        ==/("<table><tr><th>1-1</th><th>1-2</th></tr><tr><td>2-1</td><td>2-2</td></tr></table>")
    }
  }
}
