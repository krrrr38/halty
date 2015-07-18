package com.krrrr38.halty

import org.specs2.matcher.ParserMatchers
import org.specs2.mutable.Specification

class HaltyParserSpec extends Specification with ParserMatchers {

  object HaltyTestParser extends HaltyParser

  override val parsers = HaltyTestParser

  import HaltyTestParser._

  // body test in HaltySpec

  "header" should {
    "match with 1 to 3 asters" in {
      header must succeedOn("*aaa").withResult(Header(H3, Inline("aaa")))
      header must succeedOn("* aaa").withResult(Header(H3, Inline(" aaa")))
      header must succeedOn("** aaa").withResult(Header(H4, Inline(" aaa")))
      header must succeedOn("*** aaa").withResult(Header(H5, Inline(" aaa")))
      header must succeedOn("**** aaa").withResult(Header(H5, Inline("* aaa")))
    }
    "fail without aster" in {
      header must failOn("aaa")
    }
  }

  "blockquote" should {
    val texts = s"aaa\n\nbbb\nwaiwai"
    "match with texts" in {
      val expect = BlockQuote(List(
        Paragraph(List(Inline("aaa"))), Empty,
        Paragraph(List(Inline("bbb"))), Paragraph(List(Inline("waiwai")))))
      blockQuote must succeedOn(s">>\n$texts\n<<").withResult(expect)
    }
    "match with texts and url" in {
      val url = "http://hidamari.local"
      val expect = (http: Http) => BlockQuote(List(
        Paragraph(List(Inline("aaa"))), Empty,
        Paragraph(List(Inline("bbb"))), Paragraph(List(Inline("waiwai")))
      ), Some(http))
      blockQuote must succeedOn(s">$url>\n$texts\n<<")
        .withResult(expect(HttpRawTitle(url, url)))
      blockQuote must succeedOn(s">[$url]>\n$texts\n<<")
        .withResult(expect(HttpRawTitle(url, url)))
      blockQuote must succeedOn(s">[$url:title]>\n$texts\n<<")
        .withResult(expect(HttpAutoTitle(url)))
      blockQuote must succeedOn(s">[$url:title=foo]>\n$texts\n<<")
        .withResult(expect(HttpRawTitle(url, "foo")))
    }
    "fail with invalid format" in {
      blockQuote must failOn(s">>\n$texts\n<")
      blockQuote must failOn(s">>\n$texts<<")
    }
  }

  "dl" should {
    val def1 = s"yunotti"
    val detail1 = s"miyachan-waiwai"
    val def2 = s"hiro"
    val detail2 = s"sae-san"
    "match with single definition" in {
      dl must succeedOn(s":$def1:$detail1")
        .withResult(DL(List(DLItem(def1, Inline(detail1)))))
    }
    "match with multiple definitions" in {
      dl must succeedOn(s":$def1:$detail1\n:$def2:$detail2:aaa")
        .withResult(DL(List(
          DLItem(def1, Inline(detail1)),
          DLItem(def2, Inline(detail2 + ":aaa")))))
    }
  }

  "dlitem" should {
    val definition = s"yunotti"
    val detail = s"miyachan-waiwai"
    "match in simple case" in {
      dlitem must succeedOn(s":$definition:$detail")
        .withResult(DLItem(definition, Inline(detail)))
    }
    "match with too much delimiter" in {
      dlitem must succeedOn(s":$definition:$detail:aaa")
        .withResult(DLItem(definition, Inline(detail + ":aaa")))
    }
    "fail without detail" in {
      dlitem must failOn(s":$definition")
      dlitem must failOn(s":$definition:")
    }
  }

  "list" should {
    "match one ulline" in {
      list(1) must succeedOn("-foo").withResult(UL(1, List(LI(Inline("foo"), None))))
    }
    "match one oline" in {
      list(1) must succeedOn("+foo").withResult(OL(1, List(LI(Inline("foo"), None))))
    }
    "match multiple ullines" in {
      list(1) must succeedOn("-foo\n--bar\n-piyo")
        .withResult(
          UL(1, List(
            LI(Inline("foo"),
              Some(UL(2, List(LI(Inline("bar"), None))))),
            LI(Inline("piyo"), None))))
    }
    "match multiple ollines" in {
      list(1) must succeedOn("+foo\n++bar\n+piyo")
        .withResult(
          OL(1, List(
            LI(Inline("foo"),
              Some(OL(2, List(LI(Inline("bar"), None))))),
            LI(Inline("piyo"), None))))
    }
    "match multiple complex lines" in {
      list(1) must succeedOn("+foo\n--bar\n+piyo")
        .withResult(
          OL(1, List(
            LI(Inline("foo"),
              Some(UL(2, List(LI(Inline("bar"), None))))),
            LI(Inline("piyo"), None))))
    }
    "fail with invalid level" in {
      list(2) must failOn("+foo")
    }
    "fail with empty line" in {
      list(1) must failOn("+foo\n\n+foo")
    }
  }

  "ulitem" should {
    "match one line(same level)" in {
      ulitem(1) must succeedOn("-foo").withResult(LI(Inline("foo"), None))
      ulitem(1) must succeedOn("--foo").withResult(LI(Inline("-foo"), None))
    }
    "match with next level list" in {
      ulitem(1) must succeedOn("-foo\n++bar")
        .withResult(
          LI(Inline("foo"), Some(
            OL(2, List(LI(Inline("bar"), None))))))
    }
    "fail with low level ulitem" in {
      ulitem(3) must failOn("-foo")
    }
    "fail with olitem" in {
      ulitem(1) must failOn("+foo")
    }
  }

  "olitem" should {
    "match one line(same level)" in {
      olitem(1) must succeedOn("+foo").withResult(LI(Inline("foo"), None))
      olitem(1) must succeedOn("++foo").withResult(LI(Inline("+foo"), None))
    }
    "match with next level list" in {
      olitem(1) must succeedOn("+foo\n--bar")
        .withResult(
          LI(Inline("foo"), Some(
            UL(2, List(LI(Inline("bar"), None))))))
    }
    "fail with low level olitem" in {
      olitem(3) must failOn("+foo")
    }
    "fail with ulitem" in {
      olitem(1) must failOn("-foo")
    }
  }

  "super pre" should {
    val texts = "aaa\n\nbbb"
    "match with text" in {
      superPre must succeedOn(s">||\naaa\n||<").withResult(SuperPre("aaa", None))
    }
    "match with texts" in {
      superPre must succeedOn(s">||\n$texts\n||<").withResult(SuperPre(texts, None))
    }
    "match with texts and lang" in {
      val expect = (lang: Option[String]) => SuperPre(texts, lang)
      superPre must succeedOn(s">|java|\n$texts\n||<").withResult(expect(Some("java")))
      superPre must succeedOn(s">|java ???|\n$texts\n||<").withResult(expect(Some("java ???")))
    }
    "fail without close bracket" in {
      superPre must failOn(s">|java ???|\n$texts\n|<")
    }
  }

  "pre" should {
    "match with text" in {
      pre must succeedOn(">|\naaa\n|<").withResult(Pre(List(Paragraph(List(Inline("aaa"))))))
    }
    "match with blocks" in {
      val texts = s"aaa\n\n- bbb\n-- ccc\n"
      val expect = Pre(List(
        Paragraph(List(Inline("aaa"))), Empty,
        UL(1, List(LI(Inline(" bbb"), Some(UL(2, List(LI(Inline(s" ccc"), None)))))))
      ))
      pre must succeedOn(s">|\n$texts|<").withResult(expect)
    }
  }

  "table row" should {
    val str1 = s"yunotti"
    val str2 = s"miyachan waiwai"
    // to detect all cells, "\n" is necessary.
    "match with a cell" in {
      tableRow <~ "\n" must succeedOn(s"|$str1|\n")
        .withResult(TableRow(List(TableData(Inline(str1)))))
    }
    "match with cells" in {
      tableRow <~ "\n" must succeedOn(s"|$str1|* $str2|*$str1|\n")
        .withResult(TableRow(List(TableData(Inline(str1)), TableData(Inline(s" $str2"), true), TableData(Inline(str1), true))))
    }
    "match with empty cell" in {
      tableRow <~ "\n" must succeedOn(s"|$str1||*$str2|\n")
        .withResult(TableRow(List(TableData(Inline(str1)), TableData(Inline("")), TableData(Inline(str2), true))))
    }
    "fail with invalid cel" in {
      tableRow <~ "\n" must failOn(s"|$str1|* $str2|*$str1\n")
    }
  }

  "inline" should {
    val str = "yuno"
    "match with only text" in {
      inline must succeedOn(str).withResult(Inline(str))
      inline must succeedOn(str + "  " + str).withResult(Inline(str + "  " + str))
    }
    "fail with empty text" in {
      inline must failOn("")
    }
  }

  "http" should {
    val url = s"http://hidamari.local"
    val param = s"?aaa=bbb"
    val title = s"title-waiwai"
    "match without title" in {
      http must succeedOn(url).withResult(HttpRawTitle(url, url))
      http must succeedOn(url + param).withResult(HttpRawTitle(url + param, url + param))
      http must succeedOn(s"[$url]").withResult(HttpRawTitle(url, url))
      http must succeedOn(s"[$url$param]").withResult(HttpRawTitle(url + param, url + param))
    }
    "match with auto detected title" in {
      http must succeedOn(s"[$url:title]").withResult(HttpAutoTitle(url))
      http must succeedOn(s"[$url$param:title]").withResult(HttpAutoTitle(url + param))
    }
    "match with title" in {
      http must succeedOn(s"[$url:title=$title]").withResult(HttpRawTitle(url, title))
      http must succeedOn(s"[$url$param:title=$title]").withResult(HttpRawTitle(url + param, title))
    }
    "fail with invalid patterns" in {
      http must failOn("htttp://hogehoge.com")
      http must failOn("//hogehoge.com")
      http must failOn("[http://hogehoge.com")
    }
  }

  "empty" should {
    "match empty text" in {
      parsers.empty must succeedOn("").withResult(Empty)
    }
    "fail with some words" in {
      parsers.empty must failOn("a")
      parsers.empty must failOn("\n")
    }
  }

  "newLine" should {
    "match with only line break" in {
      newLine must succeedOn("\n")
      newLine must succeedOn("\r\n")
    }
    "fail with text" in {
      newLine must failOn("a\n")
      newLine must failOn("\r")
    }
  }
}
