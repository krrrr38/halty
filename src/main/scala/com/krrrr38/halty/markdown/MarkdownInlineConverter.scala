package com.krrrr38.halty.markdown

import com.krrrr38.halty.{ DefaultInlineConverter, Fetcher }

class MarkdownInlineConverter(val fetcher: Fetcher) extends DefaultInlineConverter[String] {
  def text(text: String) = text

  def mail(address: String) = s"[$address](mailto:$address)"

  def wrap(seq: Seq[String]) = seq.mkString("\n")

  def image(url: String, maybeWidth: Option[String], maybeHeight: Option[String]) = (maybeWidth, maybeHeight) match {
    case (Some(width), Some(height)) => s"![]($url =${width}x${height})"
    case (Some(width), None) => s"![]($url =${width}x)"
    case (None, Some(height)) => s"![]($url =x${height})"
    case (None, None) => s"![]($url)"
  }

  def http(url: String, title: String) = s"[$title]($url)"
}
