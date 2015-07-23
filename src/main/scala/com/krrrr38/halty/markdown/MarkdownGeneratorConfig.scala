package com.krrrr38.halty.markdown

import com.krrrr38.halty._

trait MarkdownGeneratorConfig extends GeneratorConfig {

  /**
   * If true, get title using `fetcher` for `HttpAutoTitle`
   */
  val withAutoTitle: Boolean

  /**
   * If true, table's first row would be treated as header.
   */
  val withTableHeader: Boolean

  /**
   * AutoLinker for `Inline`.
   */
  val inlineConverter: InlineConverter[String]
}

trait DefaultMarkdownGeneratorConfig extends MarkdownGeneratorConfig {

  /**
   * If true, get title using `fetcher` for `HttpAutoTitle`.<br />
   * default is false.
   */
  val withAutoTitle: Boolean = false

  /**
   * If true, table's first row would be treated as header.<br />
   * default is true.
   */
  val withTableHeader: Boolean = true

  /**
   * get title for `HttpAutoTitle`. If `withAutoTitle` is false, never used.
   */
  val fetcher: Fetcher = DefaultJsoupFetcher

  /**
   * AutoLinker for `Inline`
   * @return
   */
  lazy val inlineConverter: InlineConverter[String] = new MarkdownInlineConverter(fetcher)
}

object DefaultMarkdownGeneratorConfig extends DefaultMarkdownGeneratorConfig
