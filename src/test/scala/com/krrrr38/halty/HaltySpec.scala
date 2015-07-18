package com.krrrr38.halty

import org.scalacheck.{ Arbitrary, Gen }
import org.specs2.ScalaCheck
import org.specs2.matcher.ParserMatchers
import org.specs2.mutable.Specification

class HaltyCheck extends Specification with ScalaCheck {
  override def is = s2"""
  Haltyapply must success with
    any input strings           $anyinput
  """

  def haltySymbolGen: Gen[String] = Gen.oneOf("http://", "http://hodamari.local/", ">", "|", "^", "<", "*", " ", "　", ":", "\n", "\t", "\r", "/", "[", "]")

  def haltyStringGen: Gen[String] = Gen.frequency((1, Gen.alphaStr), (5, haltySymbolGen))

  def haltyStringsGen: Gen[String] = Gen.listOf(haltyStringGen).map(_.mkString)

  def anyinput = prop { (input: String) =>
    Halty(input) must not be empty
  }
    .set(minTestsOk = 300, maxSize = 200, workers = 5)
    .setArbitrary(Arbitrary(haltyStringsGen))
}

class HaltySpec extends Specification with ParserMatchers {
  override val parsers = Halty

  // see also HaltyCheck
  "apply" should {
    "merge continuoous paragraphs" in {
      Halty("aaa\nbbb\n") must_== List(
        Paragraph(List(Inline("aaa"), Inline("bbb"))))
    }
    "merge continuoous tablerows into table" in {
      Halty("|a|b|\n|100|*200|\n") must_== List(
        Table(List(
          TableRow(List(TableData(Inline("a")), TableData(Inline("b")))),
          TableRow(List(TableData(Inline("100")), TableData(Inline("200"), true))))))
    }
    "success if the last words is not break line" in {
      Halty("aaa\n   ") must_== List(
        Paragraph(List(Inline("aaa"), Inline("   "))))
    }
    "success with empty string" in {
      Halty("") must_== List(Empty)
    }
  }

  "body" should {
    "match with only texts" in {
      Halty.body must succeedOn("aaa\nbbb\n")
        .withResult(List(
          Paragraph(List(Inline("aaa"))), Paragraph(List(Inline("bbb")))))
    }
    "match with empty lines" in {
      Halty.body must succeedOn("aaa\n\n\n")
        .withResult(List(
          Paragraph(List(Inline("aaa"))), Empty, Empty))
    }
    "match with space line" in {
      Halty.body must succeedOn("  \n")
        .withResult(List(Paragraph(List(Inline("  ")))))
    }
    "match with multiple contents" in {
      val text =
        """|* header
          |>[http://hidamari.local:title=yuno]>
          |foo あああ
          |
          |- bar
          |-- bar
          |-- bar
          |--- bar
          |- bar
          |- bar
          |<<
          |
          ||* aaa|* bbb|
          || 100 | 200 |
          |
          |** code
          |>|scala|
          |trait Foo {
          |  def foo: String
          |}
          |case class Bar(bar: String) extends Foo {
          |  def foo: Strinig = bar
          |}
          |||<
          |
          |
          |:def1:detail:rest
          |:def2:detail2
          |
          |simple words
          |simple words
          |""".stripMargin
      val expected = List(
        Header(H3, Inline(" header")),
        BlockQuote(List(
          Paragraph(List(Inline("foo あああ"))),
          Empty,
          UL(1, List(
            LI(Inline(" bar"), Some(
              UL(2, List(
                LI(Inline(" bar"), None),
                LI(Inline(" bar"), Some(
                  UL(3, List(
                    LI(Inline(" bar"), None))))))))),
            LI(Inline(" bar"), None),
            LI(Inline(" bar"), None)))
        ), Some(HttpRawTitle("http://hidamari.local", "yuno"))),
        Empty,
        TableRow(List(TableData(Inline(" aaa"), true), TableData(Inline(" bbb"), true))),
        TableRow(List(TableData(Inline(" 100 "), false), TableData(Inline(" 200 "), false))),
        Empty,
        Header(H4, Inline(" code")),
        SuperPre(
          """|trait Foo {
            |  def foo: String
            |}
            |case class Bar(bar: String) extends Foo {
            |  def foo: Strinig = bar
            |}""".stripMargin, Some("scala")),
        Empty,
        Empty,
        DL(List(DLItem("def1", Inline("detail:rest")), DLItem("def2", Inline("detail2")))),
        Empty,
        Paragraph(List(Inline("simple words"))),
        Paragraph(List(Inline("simple words")))
      )
      Halty.body must succeedOn(text).withResult(expected)
    }
    "fail with last word is not break line (Halty.apply help this)" in {
      Halty.body must failOn("aa\n ")
    }
  }
}
