package com.blinkbox.books.spray

object Paging {

  /**
   * Values for a page link as included in responses.
   */
  case class PageLink(rel: String, href: String)

  /**
   * Generate links for use in paged results.
   *
   * @param numberOfResults The number of total results that exist. If present, this will
   * be used to decide whether to generate a "next" link or not. If not present, the "next" link
   * will always be generated.
   * @param offset The current offset into the results list.
   * @param count The number of results in a page.
   */
  def links(numberOfResults: Option[Int], offset: Int, count: Int,
    linkBaseUrl: String, includeSelf: Boolean = true): Seq[PageLink] = {
    val hasMore = numberOfResults.fold(true)(_ > offset + count)
    val thisPage = optLink(includeSelf, PageLink("this", s"$linkBaseUrl?count=$count&offset=$offset"))
    val prevPage = optLink(offset > 0, PageLink("prev", s"$linkBaseUrl?count=$count&offset=${(offset - count).max(0)}"))
    val nextPage = optLink(hasMore, PageLink("next", s"$linkBaseUrl?count=$count&offset=${offset + count}"))
    Seq(thisPage, prevPage, nextPage).flatten
  }

  private def optLink(cond: Boolean, link: => PageLink) = if (cond) Some(link) else None
}