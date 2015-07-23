package com.krrrr38.halty.markdown

import com.krrrr38.halty.{ Inline, Fetcher }
import org.specs2.mutable.Specification

class MarkdownInlineConverterTest extends Specification {
  val identityFetcher = new Fetcher {
    override def fetchTitle(url: String) = url
  }
  val converter = new MarkdownInlineConverter(identityFetcher)

  "MarkdownInlineConverterTest" should {
    "wrap" in {
      converter.wrap(Seq("a", "b")) must ==/("a\nb")
    }
    "text" in {
      converter.text("aaa") must ==/("aaa")
      converter.text("  aaa") must ==/("  aaa")
      converter.text("a aa  ") must ==/("aaa  ")
    }
    "escape text" in {
      val raw = "aaa > < & ' bbb"
      converter.text(raw) must ==/(raw)
    }
    "image" in {
      val image = "http://hidamari.local/yuno.png"
      converter.image(image, None, None) must ==/(s"""![]($image)""")
      converter.image(image, None, Some("200")) must ==/(s"""![]($image =x200)""")
      converter.image(image, Some("200"), None) must ==/(s"""![]($image =200x)""")
    }
    "http" in {
      val url = "http://hidamari.local"
      val title = "hidamari sketch"
      converter.http(url, title) must ==/(s"[$title]($url)")
      val urlWithParameter = "http://hidamari.local/foo/bar?aaa=a%20a&hoge=fuga"
      converter.http(urlWithParameter, title) must ==/(s"[$title]($urlWithParameter)")
    }
    "mail" in {
      val mail = "yuno@hidamari.local"
      converter.mail(mail) must ==/(s"[$mail](mailto:$mail)")
    }

    // convert ///////////////////////////////////////////////////////////////////////////////
    "convert image" in {
      val image = "http://hidamari.local/yuno.png"
      converter.convert(Inline(s"aaa [$image:image]")) must ==/(s"""aaa ![]($image)""")
      converter.convert(Inline(s"aaa [$image:image:h200]")) must ==/(s"""aaa ![]($image =x200)""")
      converter.convert(Inline(s"aaa [$image:image:w200]")) must ==/(s"""aaa ![]($image =200x)""")
    }
    "convert http" in {
      val url = "http://hidamari.local"
      converter.convert(Inline(s"aaa $url bbb")) must ==/(s"""aaa [$url]($url) bbb""")
      val urlWithParameter = "http://hidamari.local/foo/bar?aaa=a%20a&hoge=fuga"
      converter.convert(Inline(s"aaa $urlWithParameter bbb")) must ==/(s"""aaa [$urlWithParameter]($urlWithParameter) bbb""")
    }
    "convert bracket http" in {
      val url = "http://hidamari.local"
      converter.convert(Inline(s"aaa [$url] bbb")) must ==/(s"""aaa [$url]($url) bbb""")
      converter.convert(Inline(s"aaa [$url:title] bbb")) must ==/(s"""aaa [$url]($url) bbb""")
      converter.convert(Inline(s"aaa [$url:title=foo] bbb")) must ==/(s"""aaa [foo]($url) bbb""")
    }
    "convert multiple" in {
      val mail1 = "yuno@hidamari.local"
      val mail2 = "miyako@hidamari.local"
      val url = "http://hidamari.local"
      // start & end with contents
      val text1 = s"mailto:$mail1 [$url:title=foo] mailto:$mail2 $url"
      val expected1 =
        s"""[$mail1](mailto:$mail1)
           |[foo]($url)
           |[$mail2](mailto:$mail2)
           |[$url]($url)
           |""".stripMargin.replaceAll("\n", "")
      converter.convert(Inline(text1)) must ==/(expected1)
      // start & end with words
      val text2 = s"aaa mailto:$mail1 [$url:title=foo] mailto:$mail2 $url aaa"
      val expected2 =
        s"""aaa
           |[$mail1](mailto:$mail1)
           |[foo]($url)
           |[$mail2](mailto:$mail2)
           |[$url]($url)
           |aaa
         """.stripMargin.replaceAll("\n", "")
      converter.convert(Inline(text2)) must ==/(expected2)
    }
  }
}