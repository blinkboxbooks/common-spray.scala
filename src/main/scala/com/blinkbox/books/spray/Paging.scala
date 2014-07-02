package com.blinkbox.books.spray

import spray.http.Uri

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
  def links(numberOfResults: Option[Int], offset: Int, count: Int, linkBaseUrl: String,
            linkParams: Option[Map[String, String]] = None, includeSelf: Boolean = true): Seq[PageLink] = {
    val params = linkParams ++ Map("count" -> count, "offset" -> count)
    val hasMore = numberOfResults.fold(true)(_ > offset + count)
    val thisPage = optLink(includeSelf, getPageLink("this", linkBaseUrl, linkParams, count, offset))
    val prevPage = optLink(offset > 0, getPageLink("prev", linkBaseUrl, linkParams, count, (offset - count).max(0)))
    val nextPage = optLink(hasMore, getPageLink("next", linkBaseUrl, linkParams, count, offset + count))
    Seq(thisPage, prevPage, nextPage).flatten
  }

  private def getPageLink(rel: String, linkBaseUrl: String, linkParams: Option[Map[String, String]], count: Int, offset: Int): PageLink = {
    val params = linkParams.getOrElse(Map.empty) ++ Map("count" -> count.toString, "offset" -> offset.toString)
    PageLink(rel, Uri(linkBaseUrl).withQuery(params).toString())
  }

  private def optLink(cond: Boolean, link: => PageLink) = if (cond) Some(link) else None
}
