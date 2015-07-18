package com.krrrr38.halty.xhtml

import com.krrrr38.halty.{ Fetcher, Inline }

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex.Match
import scala.xml.{ Utility, Group, Text, Node }

trait InlineConverter {
  /**
   * convert raw text to Node such as auto link
   * @param inline
   * @return
   */
  def convert(inline: Inline): Node

  /**
   * html escape by using scala.xml.Utility
   * @param text
   * @return
   */
  def escape(text: String): String = Utility.escape(text)
}

class DefaultInlineConverter(fetcher: Fetcher) extends InlineConverter {

  // order means priority
  lazy val SYNTAXES = List(
    """\[(https?:\/\/[A-Za-z0-9~\/._\?\&=\-%#\+:\;,\@\']+(?:jpg|jpeg|gif|png|bmp)):image(:[hw]\d+)?\]""".r -> httpImage,
    """\[(https?:\/\/[A-Za-z0-9~\/._\?\&=\-%#\+:\;,\@\']+):title\]""".r -> httpAutoTitle,
    """\[(https?:\/\/[A-Za-z0-9~\/._\?\&=\-%#\+:\;,\@\']+):title=([^\]]+)\]""".r -> httpRawTitle,
    """(?:\[)?(https?:\/\/[A-Za-z0-9~\/._\?\&=\-%#\+:\;,\@\']+)(?:\])?""".r -> httpRawTitle,
    """(?:\[)?mailto:(\w[\w\.-]+\@\w[\w\.\-]*\w)(?:\])?""".r -> mail
  )

  /**
   * convert raw text to Node such as auto link.
   * based on https://metacpan.org/source/Text::Hatena::AutoLink
   * @param inline
   * @return
   */
  override def convert(inline: Inline): Node = {
    var text = inline.text
    val end = text.length
    val buffer = ArrayBuffer.empty[Node]

    /**
     * find match syntax. If multiple syntax regexex are matched, consider less start point.
     * If they have same start points, the longer one is prefered.
     * @param input
     * @return
     */
    def findMinStartSyntax(input: String): Option[(Match, Match => Node)] =
      SYNTAXES.foldLeft(((end, 0), None): ((Int, Int), Option[(Match, Match => Node)])) {
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

    while (text.length > 0) {
      findMinStartSyntax(text) match {
        case Some((matcher, rule)) =>
          if (matcher.start > 0) {
            buffer += Text(text.substring(0, matcher.start))
          }
          buffer += rule(matcher)
          text = text.substring(matcher.end)
        case None =>
          buffer += Text(text)
          text = ""
      }
    }

    Group(buffer)
  }

  private val httpImage: Match => Node = (matcher: Match) => {
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
    <a href={ url }><img src={ url } alt={ url } height={ maybeHeight.orNull } width={ maybeWidth.orNull }/></a>
  }

  private val httpRawTitle: Match => Node = (matcher: Match) => {
    val url = matcher.group(1)
    if (matcher.groupCount == 2) {
      val title = matcher.group(2)
      <a href={ url }>{ title }</a>
    } else {
      <a href={ url }>{ url }</a>
    }
  }

  private val httpAutoTitle: Match => Node = (matcher: Match) => {
    val url = matcher.group(1)
    val title = fetcher.fetchTitle(url)
    <a href={ url }>{ title }</a>
  }

  private val mail: Match => Node = (matcher: Match) => {
    val address = matcher.group(1)
    val mailto = s"mailto:$address"
    <a href={ mailto }>{ address }</a>
  }

}