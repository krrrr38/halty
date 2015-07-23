package com.krrrr38.halty.xhtml

import com.krrrr38.halty.{ DefaultInlineConverter, Fetcher }

import scala.xml.{ Group, Node, Text, Utility }

class XHTMLInlineConverter(val fetcher: Fetcher) extends DefaultInlineConverter[Node] {
  def wrap(seq: Seq[Node]) = Group(seq)

  def text(text: String): Node = Text(text)

  def http(url: String, title: String): Node =
    <a href={ url }>{ title }</a>

  def image(url: String, maybeWidth: Option[String], maybeHeight: Option[String]): Node =
    <a href={ url }><img src={ url } alt={ url } height={ maybeHeight.orNull } width={ maybeWidth.orNull }/></a>

  def mail(address: String): Node =
    <a href={ s"mailto:$address" }>{ address }</a>

  /**
   * html escape by using scala.xml.Utility
   * @param text
   * @return
   */
  def escape(text: String): String = Utility.escape(text)
}
