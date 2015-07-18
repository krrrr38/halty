package com.krrrr38.halty.xhtml

import com.krrrr38.halty.{ Inline, Fetcher }
import org.specs2.mutable.Specification

import scala.xml.Utility

class DefaultInlineConverterTest extends Specification {
  val identityFetcher = new Fetcher {
    override def fetchTitle(url: String) = url
  }
  val converter = new DefaultInlineConverter(identityFetcher)

  def escape(text: String) = Utility.escape(text)

  "convert" should {
    "text" in {
      converter.convert(Inline("aaa")) must ==/("aaa")
      converter.convert(Inline("  aaa")) must ==/("  aaa")
      converter.convert(Inline("a aa  ")) must ==/("aaa  ")
    }
    "escape text" in {
      val raw = "aaa > < & ' bbb"
      converter.convert(Inline(raw)) must ==/(escape(raw))
    }
    "image" in {
      val image = "http://hidamari.local/yuno.png"
      converter.convert(Inline(s"aaa [$image:image]")) must
        ==/(s"""aaa<a href="${escape(image)}"><img src="${escape(image)}" alt="${escape(image)}"/></a>""")
      converter.convert(Inline(s"aaa [$image:image:h200]")) must
        ==/(s"""aaa<a href="${escape(image)}"><img src="${escape(image)}" alt="${escape(image)}" height="200"/></a>""")
      converter.convert(Inline(s"aaa [$image:image:w200]")) must
        ==/(s"""aaa<a href="${escape(image)}"><img src="${escape(image)}" alt="${escape(image)}" width="200"/></a>""")
    }
    "http" in {
      val url = "http://hidamari.local"
      converter.convert(Inline(s"aaa $url bbb")) must ==/(s"""aaa<a href="${escape(url)}">${escape(url)}</a>bbb""")
      val urlWithParameter = "http://hidamari.local/foo/bar?aaa=a%20a&hoge=fuga"
      converter.convert(Inline(s"aaa $urlWithParameter bbb")) must ==/(s"""aaa<a href="${escape(urlWithParameter)}">${escape(urlWithParameter)}</a>bbb""")
    }
    "bracket http" in {
      val url = "http://hidamari.local"
      converter.convert(Inline(s"aaa [$url] bbb")) must ==/(s"""aaa<a href="${escape(url)}">${escape(url)}</a>bbb""")
      converter.convert(Inline(s"aaa [$url:title] bbb")) must ==/(s"""aaa<a href="${escape(url)}">${escape(url)}</a>bbb""")
      converter.convert(Inline(s"aaa [$url:title=foo] bbb")) must ==/(s"""aaa<a href="${escape(url)}">foo</a>bbb""")
    }
    "mail" in {
      val mail = "yuno@hidamari.local"
      converter.convert(Inline(s"aaa mailto:$mail")) must ==/(s"""aaa<a href="mailto:${escape(mail)}">${escape(mail)}</a>""")
    }
    "multiple" in {
      val mail1 = "yuno@hidamari.local"
      val mail2 = "miyako@hidamari.local"
      val url = "http://hidamari.local"
      // start & end with contents
      val text1 = s"mailto:$mail1 [$url:title=foo] mailto:$mail2 $url"
      val expected1 =
        s"""
           |<a href="mailto:${escape(mail1)}">${escape(mail1)}</a>
           |<a href="${escape(url)}">foo</a>
           |<a href="mailto:${escape(mail2)}">${escape(mail2)}</a>
           |<a href="${escape(url)}">${escape(url)}</a>
           |""".stripMargin.replaceAll("\n", "")
      converter.convert(Inline(text1)) must ==/(expected1)
      // start & end with words
      val text2 = s"aaa mailto:$mail1 [$url:title=foo] mailto:$mail2 $url aaa"
      val expected2 =
        s"""aaa
           |<a href="mailto:${escape(mail1)}">${escape(mail1)}</a>
           |<a href="${escape(url)}">foo</a>
           |<a href="mailto:${escape(mail2)}">${escape(mail2)}</a>
           |<a href="${escape(url)}">${escape(url)}</a>
           |aaa
           |""".stripMargin.replaceAll("\n", "")
      converter.convert(Inline(text2)) must ==/(expected2)
    }
  }
}
