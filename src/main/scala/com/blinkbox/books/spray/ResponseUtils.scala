package com.blinkbox.books.spray

object ResponseUtils {

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

    val thisPage = if (includeSelf) {
      val link = s"$linkBaseUrl?count=$count&offset=$offset"
      Some(PageLink("this", link))
    } else None

    val previousPage = if (offset > 0) {
      val link = s"$linkBaseUrl?count=$count&offset=${(offset - count).max(0)}"
      Some(PageLink("prev", link))
    } else None

    val hasMore = numberOfResults.fold(true)(_ > offset + count)
    val nextPage = if (hasMore) {
      val link = s"$linkBaseUrl?count=$count&offset=${offset + count}"
      Some(PageLink("next", link))
    } else None

    Seq(thisPage, previousPage, nextPage).flatten
  }

}