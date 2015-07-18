package com.krrrr38.halty.xhtml

import com.krrrr38.halty._

import scala.xml.{ Group, Node }

object XHTMLGenerator extends Generator[Node, XHTMLGeneratorConfig] {
  /**
   * Generate contents from Chunk list. If HttpAutoTitle contents are exists,
   * fetcher will get the page title through http request.
   * @param blocks
   * @param config
   * @return
   */
  override def generate(blocks: List[Block], config: XHTMLGeneratorConfig): Node =
    new XHTMLGenerator(config).generate(blocks)

  /**
   * Generate contents from Chunk list. If HttpAutoTitle contents are exists,
   * fetcher will get the page title through http request.
   * @param blocks
   * @return
   */
  def generate(blocks: List[Block]): Node = generate(blocks, DefaultXHTMLGeneratorConfig)
}

class XHTMLGenerator(config: XHTMLGeneratorConfig) {

  def generate(blocks: List[Block]): Node =
    config.wrap(Group(blocks.map(blockToXHTML)))

  private[this] def blockToXHTML(block: Block): Node = block match {
    case header: Header => headerToXHTML(header)
    case blockQuote: BlockQuote => blockQuoteToXHTML(blockQuote)
    case list: ListTag => listToXHTML(list)
    case dl: DL => dlToXHTML(dl)
    case Empty => <br/>
    case paragraph: Paragraph => paragraphToXHTML(paragraph)
    case pre: Pre => preToXHTML(pre)
    case superPre: SuperPre => superPreToXHTML(superPre)
    case table: Table => tableToXHTML(table)
    case tableRow: TableRow => tableRowToXHTML(tableRow)
  }

  private[this] def headerToXHTML(header: Header): Node = {
    val content = inlineToXHTML(header.inline)
    header.tag match {
      case H3 => <h3>{ content }</h3>
      case H4 => <h4>{ content }</h4>
      case H5 => <h5>{ content }</h5>
    }
  }

  private[this] def blockQuoteToXHTML(blockquote: BlockQuote): Node = {
    val contents = Group(blockquote.blocks.map(blockToXHTML))
    blockquote.http match {
      case Some(http) =>
        <blockquote cite={ http.url }>{ contents }<cite>{ httpToXHTML(http) }</cite></blockquote>
      case None => <blockquote>{ contents } </blockquote>
    }
  }

  private[this] def listToXHTML(list: ListTag): Node = list match {
    case ol: OL => <ol>{ ol.items.map(listitemToXHTML) }</ol>
    case ul: UL => <ul>{ ul.items.map(listitemToXHTML) }</ul>
  }

  private[this] def listitemToXHTML(item: LI): Node = item.nest match {
    case Some(nestedList) => <li>{ inlineToXHTML(item.inline) }{ listToXHTML(nestedList) }</li>
    case None => <li>{ inlineToXHTML(item.inline) }</li>
  }

  private[this] def dlToXHTML(dl: DL): Node =
    <dl>{ dl.items.map(dlItemToXHTML) }</dl>

  private[this] def dlItemToXHTML(item: DLItem): Node = Group(Seq(
    <dt>{ item.definition }</dt>,
    <dd>{ inlineToXHTML(item.detail) }</dd>
  ))

  private[this] def paragraphToXHTML(paragraph: Paragraph): Node =
    <p>{ Group(paragraph.inlines.map(inlineToXHTML)) }</p>

  private[this] def preToXHTML(pre: Pre): Node =
    <pre>{ Group(pre.blocks.map(blockToXHTML)) }</pre>

  private[this] def superPreToXHTML(superpre: SuperPre): Node = {
    val preClass = config.codeClass(superpre.lang)
    <pre class={ preClass.orNull }>{ superpre.content }</pre>
  }

  private[this] def tableToXHTML(table: Table): Node =
    <table>{ Group(table.rows.map(tableRowToXHTML)) }</table>

  private[this] def tableRowToXHTML(row: TableRow): Node =
    <tr>{ row.cells.map(tableDataToXHTML) }</tr>

  private[this] def tableDataToXHTML(data: TableData): Node =
    if (data.isEmphasis)
      <th>{ inlineToXHTML(data.inline) }</th>
    else
      <td>{ inlineToXHTML(data.inline) }</td>

  private[this] def inlineToXHTML(inline: Inline): Node =
    config.inlineConverter.convert(inline)

  private[this] def httpToXHTML(http: Http): Node = http match {
    case HttpRawTitle(url, title) => <a href={ url }>{ title }</a>
    case HttpAutoTitle(url) =>
      val title = config.fetcher.fetchTitle(url)
      <a href={ url }>{ title }</a>
  }
}
