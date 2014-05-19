package com.blinkbox.books.common.spray

object ResponseUtils {

  /**
   * Values for a page link as included in responses.
   */
  case class PageLink(rel: String, href: String)

  /**
   * Generate links for use in paged results.
   *
   * @param numberOfResults the number of total results that exist. If present, this will
   * be used to decide whether to generate a "next" link or not. If not present, the "next" link
   * will always be generated.
   *
   */
  def links(numberOfResults: Option[Long], offset: Long, count: Long,
    linkBaseUrl: String, includeSelf: Boolean = true): Seq[PageLink] = {

    val thisPageLink = s"$linkBaseUrl?count=$count&offset=$offset"
    val thisPage = if (includeSelf) Some(PageLink("this", thisPageLink)) else None

    val previousPage = if (offset > 0) {
      val link = s"$linkBaseUrl?count=$count&offset=${(offset - count).max(0)}"
      Some(PageLink("prev", link))
    } else None

    val hasMore = numberOfResults match {
      case None => true
      case Some(number) => number > offset + count
    }
    val nextPage = if (hasMore) {
      val link = s"$linkBaseUrl?count=$count&offset=${offset + count}"
      Some(PageLink("next", link))
    } else None

    Seq(thisPage, previousPage, nextPage).flatten
  }

}