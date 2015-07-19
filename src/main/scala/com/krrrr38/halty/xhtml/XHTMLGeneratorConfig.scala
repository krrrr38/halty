package com.krrrr38.halty.xhtml

import com.krrrr38.halty.{ GeneratorConfig, DefaultJsoupFetcher, Fetcher }

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
   * @return
   */
  val inlineConverter: InlineConverter
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
   * @return
   */
  lazy val inlineConverter: InlineConverter = new DefaultInlineConverter(fetcher)
}

object DefaultXHTMLGeneratorConfig extends DefaultXHTMLGeneratorConfig
