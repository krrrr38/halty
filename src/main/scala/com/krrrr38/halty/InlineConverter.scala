package com.krrrr38.halty

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex.Match

trait InlineConverter[A] {
  /**
   * convert raw text to `A` such as auto link
   * @param inline
   * @return
   */
  def convert(inline: Inline): A
}

trait DefaultInlineConverter[A] extends InlineConverter[A] {
  val fetcher: Fetcher
  def wrap(seq: Seq[A]): A
  def mail(address: String): A
  def http(url: String, title: String): A
  def image(url: String, maybeWidth: Option[String], maybeHeight: Option[String]): A
  def text(text: String): A

  // order means priority
  lazy val SYNTAXES = List(
    """\[(https?:\/\/[A-Za-z0-9~\/._\?\&=\-%#\+:\;,\@\']+(?:jpg|jpeg|gif|png|bmp)):image(:[hw]\d+)?\]""".r -> httpImageConverter,
    """\[(https?:\/\/[A-Za-z0-9~\/._\?\&=\-%#\+:\;,\@\']+):title\]""".r -> httpAutoTitleConverter,
    """\[(https?:\/\/[A-Za-z0-9~\/._\?\&=\-%#\+:\;,\@\']+):title=([^\]]+)\]""".r -> httpRawTitleConverter,
    """(?:\[)?(https?:\/\/[A-Za-z0-9~\/._\?\&=\-%#\+:\;,\@\']+)(?:\])?""".r -> httpRawTitleConverter,
    """(?:\[)?mailto:(\w[\w\.-]+\@\w[\w\.\-]*\w)(?:\])?""".r -> mailConverter
  )

  /**
   * convert raw text to `A` such as auto link.
   * based on https://metacpan.org/source/Text::Hatena::AutoLink
   * @param inline
   * @return
   */
  override def convert(inline: Inline): A = {
    var inlineText = inline.text
    val end = inlineText.length
    val buffer = ArrayBuffer.empty[A]

    /**
     * find match syntax. If multiple syntax regexex are matched, consider less start point.
     * If they have same start points, the longer one is prefered.
     * @param input
     * @return
     */
    def findMinStartSyntax(input: String): Option[(Match, Match => A)] =
      SYNTAXES.foldLeft(((end, 0), None): ((Int, Int), Option[(Match, Match => A)])) {
        case (((candStart, candEnd), cand), syntax) =>
          syntax._1.findFirstMatchIn(input) match {
            case Some(matcher) if matcher.start < candStart => ((matcher.start, matcher.end), Some((matcher, syntax._2)))
            case Some(matcher) if matcher.start == candStart =>
              if (matcher.end > candEnd) {
                ((matcher.start, matcher.end), Some((matcher, syntax._2)))
              } else {
                ((candStart, candEnd), cand)
              }
            case _ => ((candStart, candEnd), cand)
          }
      }._2

    while (inlineText.length > 0) {
      findMinStartSyntax(inlineText) match {
        case Some((matcher, rule)) =>
          if (matcher.start > 0) {
            buffer += text(inlineText.substring(0, matcher.start))
          }
          buffer += rule(matcher)
          inlineText = inlineText.substring(matcher.end)
        case None =>
          buffer += text(inlineText)
          inlineText = ""
      }
    }

    wrap(buffer)
  }

  private val httpImageConverter: Match => A = (matcher: Match) => {
    val url = matcher.group(1)
    val (maybeHeight, maybeWidth) =
      if (matcher.group(2) == null) {
        (None, None)
      } else {
        """^:([hw])(\d+)$""".r.findFirstMatchIn(matcher.group(2)) match {
          case Some(hwMatcher) =>
            if (hwMatcher.group(1) == "h") {
              (Some(hwMatcher.group(2)), None)
            } else {
              (None, Some(hwMatcher.group(2)))
            }
          case None => (None, None)
        }
      }
    image(url, maybeWidth, maybeHeight)
  }

  private val httpRawTitleConverter: Match => A = (matcher: Match) => {
    val url = matcher.group(1)
    if (matcher.groupCount == 2) {
      val title = matcher.group(2)
      http(url, title)
    } else {
      http(url, url)
    }
  }

  private val httpAutoTitleConverter: Match => A = (matcher: Match) => {
    val url = matcher.group(1)
    val title = fetcher.fetchTitle(url)
    http(url, title)
  }

  private val mailConverter: Match => A = (matcher: Match) => {
    val address = matcher.group(1)
    mail(address)
  }
}
