package com.krrrr38.halty.markdown

import com.krrrr38.halty._

object MarkdownGenerator extends Generator[String, MarkdownGeneratorConfig] {
  /**
   * Generate contents from Chunk list. If HttpAutoTitle contents are exists,
   * fetcher will get the page title through http request.
   * @param blocks
   * @param config
   * @return
   */
  override def generate(blocks: List[Block], config: MarkdownGeneratorConfig): String =
    new MarkdownGenerator(config).generate(blocks)

  /**
   * Generate contents from Chunk list. If HttpAutoTitle contents are exists,
   * fetcher will get the page title through http request.
   * @param blocks
   * @return
   */
  def generate(blocks: List[Block]): String = generate(blocks, DefaultMarkdownGeneratorConfig)
}

class MarkdownGenerator(config: MarkdownGeneratorConfig) {

  def generate(blocks: List[Block]): String = blocks.map(blockToMarkdown).mkString

  private[this] def blockToMarkdown(block: Block): String = block match {
    case header: Header => headerToMarkdown(header)
    case blockQuote: BlockQuote => blockQuoteToMarkdown(blockQuote)
    case list: ListTag => listToMarkdown(list)
    case dl: DL => dlToMarkdown(dl)
    case Empty => "\n"
    case paragraph: Paragraph => paragraphToMarkdown(paragraph)
    case pre: Pre => preToMarkdown(pre)
    case superPre: SuperPre => superPreToMarkdown(superPre)
    case table: Table => tableToMarkdown(table)
    case tableRow: TableRow => tableRowToMarkdown(tableRow)._1
  }

  private[this] def headerToMarkdown(header: Header): String = {
    val content = inlineToMarkdown(header.inline)
    // H3 ~ H5 => H2 ~ H4
    header.tag match {
      case H3 => s"## $content"
      case H4 => s"### $content"
      case H5 => s"#### $content"
    }
  }

  private[this] def blockQuoteToMarkdown(blockquote: BlockQuote): String = {
    val contents = blockquote.blocks.map(blockToMarkdown)
      .mkString("\n").split("\n")
      .map(line => s"> $line")
      .mkString
    blockquote.http match {
      case Some(http) => s"$contents\n> <cite>${httpToMarkdown(http)}</cite>"
      case None => contents
    }
  }

  private[this] def listToMarkdown(list: ListTag): String = {
    val indent = "  " * (list.level - 1)
    list match {
      case ol: OL => ol.items.map(li => s"${indent}1. ${listitemToMarkdown(li)}").mkString
      case ul: UL => ul.items.map(li => s"${indent}- ${listitemToMarkdown(li)}").mkString
    }
  }

  private[this] def listitemToMarkdown(item: LI): String = item.nest match {
    case Some(nestedList) => inlineToMarkdown(item.inline) + listToMarkdown(nestedList)
    case None => inlineToMarkdown(item.inline)
  }

  private[this] def dlToMarkdown(dl: DL): String =
    s"<dl>${dl.items.map(dlItemToMarkdown).mkString("\n")}</dl>"

  private[this] def dlItemToMarkdown(item: DLItem): String =
    s"<dt>${item.definition}</dt><dd>${inlineToMarkdown(item.detail)}</dd>"

  private[this] def paragraphToMarkdown(paragraph: Paragraph): String =
    paragraph.inlines.map(inlineToMarkdown).mkString("  \n")

  private[this] def preToMarkdown(pre: Pre): String =
    s"```\n${pre.blocks.map(blockToMarkdown).mkString("\n")}\n```"

  private[this] def superPreToMarkdown(superpre: SuperPre): String = {
    s"```${superpre.lang.getOrElse("")}\n${superpre.content}\n```"
  }

  private[this] def tableToMarkdown(table: Table): String =
    if (config.withTableHeader) {
      (table.rows.map(tableRowToMarkdown) match {
        case headerRow :: row :: tail =>
          val border = "|" + " ------ |" * headerRow._2
          headerRow._1 :: border :: row._1 :: tail.map(_._1)
        case rows => rows.map(_._1)
      }).mkString("\n")
    } else {
      table.rows.map(tableRowToMarkdown).mkString("\n")
    }

  private[this] def tableRowToMarkdown(row: TableRow): (String, Int) =
    ("|" + row.cells.map(tableDataToMarkdown).mkString("|") + "|", row.cells.size)

  private[this] def tableDataToMarkdown(data: TableData): String =
    if (data.isEmphasis) {
      s"**${inlineToMarkdown(data.inline)}**"
    } else {
      inlineToMarkdown(data.inline)
    }

  private[this] def inlineToMarkdown(inline: Inline): String =
    config.inlineConverter.convert(inline)

  private[this] def httpToMarkdown(http: Http): String = http match {
    case HttpRawTitle(url, title) => s"[$title]($url)"
    case HttpAutoTitle(url) =>
      if (config.withAutoTitle) {
        val title = config.fetcher.fetchTitle(url)
        s"[$title]($url)"
      } else {
        s"[$url]($url)"
      }
  }
}
