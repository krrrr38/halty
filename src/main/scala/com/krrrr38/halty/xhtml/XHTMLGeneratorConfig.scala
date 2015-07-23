package com.krrrr38.halty.xhtml

import com.krrrr38.halty._

import scala.xml.Node

trait XHTMLGeneratorConfig extends GeneratorConfig {
  /**
   * wrap generted xhtml
   * @param xhtml generated xhtml
   * @return
   */
  def wrap(xhtml: Node): Node

  /**
   * class for `SuperPre`
   * @param language
   * @return
   */
  def codeClass(language: Option[String]): Option[String]

  /**
   * AutoLinker for `Inline`.
   */
  val inlineConverter: InlineConverter[Node]
}

trait DefaultXHTMLGeneratorConfig extends XHTMLGeneratorConfig {
  /**
   * wrap generted xhtml
   * @param xhtml generated xhtml
   * @return
   */
  override def wrap(xhtml: Node) = xhtml

  /**
   * class for `SuperPre` based on [[http://prismjs.com/ Prism]].
   * @param language
   * @return
   */
  override def codeClass(language: Option[String]): Option[String] =
    language.map(lang => s"code language-$lang").orElse(Some("code"))

  /**
   * get title for `HttpAutoTitle`.
   */
  val fetcher: Fetcher = DefaultJsoupFetcher

  /**
   * AutoLinker for `Inline`
   */
  lazy val inlineConverter: InlineConverter[Node] = new XHTMLInlineConverter(fetcher)
}

object DefaultXHTMLGeneratorConfig extends DefaultXHTMLGeneratorConfig
