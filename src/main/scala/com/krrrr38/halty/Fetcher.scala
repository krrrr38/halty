package com.krrrr38.halty

import org.jsoup.Jsoup

import scala.util.Try

trait Fetcher {
  /**
   * fetch title of url page.
   * @param url
   */
  def fetchTitle(url: String): String
}

object DefaultJsoupFetcher extends Fetcher {
  val USER_AGENT = "Halty"
  val TIMEOUT_MILLIS_SECOND = 3000

  /**
   * fetch title of url page.
   * There is no cache, so DefaultFetcher will fetch page in every access.
   * @param url
   */
  override def fetchTitle(url: String): String =
    Try {
      Jsoup.connect(url)
        .userAgent(USER_AGENT)
        .timeout(TIMEOUT_MILLIS_SECOND)
        .get()
        .title()
    } getOrElse url
}