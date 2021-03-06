package com.blinkbox.books.spray

import com.blinkbox.books.spray.Paging._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PagingTest extends FunSuite {

  val baseUrl = "/my/base/url"

  test("links for first page, including this") {
    assert(links(numberOfResults = Some(20), offset = 0, count = 5, baseUrl, None, includeSelf = true).toSet
      == Set(PageLink("this", s"$baseUrl?count=5&offset=0"), PageLink("next", s"$baseUrl?count=5&offset=5")))
  }

  test("links for first page, excluding this") {
    assert(links(numberOfResults = Some(20), offset = 0, count = 5, baseUrl, None, includeSelf = false).toSet
      == Set(PageLink("next", s"$baseUrl?count=5&offset=5")))
  }

  test("links for middle page, including this") {
    assert(links(numberOfResults = Some(20), offset = 5, count = 5, baseUrl, None, includeSelf = true).toSet
      == Set(PageLink("this", s"$baseUrl?count=5&offset=5"),
        PageLink("prev", s"$baseUrl?count=5&offset=0"),
        PageLink("next", s"$baseUrl?count=5&offset=10")))
  }

  test("links for middle page, excluding this") {
    assert(links(numberOfResults = Some(20), offset = 5, count = 5, baseUrl, None, includeSelf = false).toSet
      == Set(PageLink("prev", s"$baseUrl?count=5&offset=0"),
        PageLink("next", s"$baseUrl?count=5&offset=10")))
  }

  test("links for last page, including this") {
    assert(links(numberOfResults = Some(20), offset = 15, count = 5, baseUrl, None, includeSelf = true).toSet
      == Set(PageLink("this", s"$baseUrl?count=5&offset=15"),
        PageLink("prev", s"$baseUrl?count=5&offset=10")))
  }

  test("links for last page, excluding this") {
    assert(links(numberOfResults = Some(20), offset = 15, count = 5, baseUrl, None, includeSelf = false).toSet
      == Set(PageLink("prev", s"$baseUrl?count=5&offset=10")))
  }

  test("links when total number isn't known") {
    // Should always include the next link in this case.
    assert(links(numberOfResults = None, offset = 15, count = 5, baseUrl, None, includeSelf = true).toSet
      == Set(PageLink("this", s"$baseUrl?count=5&offset=15"),
        PageLink("prev", s"$baseUrl?count=5&offset=10"),
        PageLink("next", s"$baseUrl?count=5&offset=20")))
  }

  test("links for first page, excluding this, including existing query parameters") {
    assert(links(numberOfResults = Some(20), offset = 0, count = 5, baseUrl, Some(Map("test" -> "1").toSeq), includeSelf = false).toSet
      == Set(PageLink("next", s"$baseUrl?test=1&count=5&offset=5")))
  }

  test("links for first page, excluding this, including existing repeated query parameters") {
    assert(links(numberOfResults = Some(20), offset = 0, count = 5, baseUrl, Some(Seq(("test", "1"), ("test", "2"))), includeSelf = false).toSet
      == Set(PageLink("next", s"$baseUrl?test=1&test=2&count=5&offset=5")))
  }

}