package com.blinkbox.books.spray

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import com.blinkbox.books.spray.Paging._

@RunWith(classOf[JUnitRunner])
class PagingTest extends FunSuite {

  val baseUrl = "/my/base/url"

  test("links for first page, including this") {
    assert(links(numberOfResults = Some(20), offset = 0, count = 5, baseUrl, includeSelf = true).toSet
      === Set(PageLink("this", s"$baseUrl?count=5&offset=0"), PageLink("next", s"$baseUrl?count=5&offset=5")))
  }

  test("links for first page, excluding this") {
    assert(links(numberOfResults = Some(20), offset = 0, count = 5, baseUrl, includeSelf = false).toSet
      === Set(PageLink("next", s"$baseUrl?count=5&offset=5")))
  }

  test("links for middle page, including this") {
    assert(links(numberOfResults = Some(20), offset = 5, count = 5, baseUrl, includeSelf = true).toSet
      === Set(PageLink("this", s"$baseUrl?count=5&offset=5"),
        PageLink("prev", s"$baseUrl?count=5&offset=0"),
        PageLink("next", s"$baseUrl?count=5&offset=10")))
  }

  test("links for middle page, excluding this") {
    assert(links(numberOfResults = Some(20), offset = 5, count = 5, baseUrl, includeSelf = false).toSet
      === Set(PageLink("prev", s"$baseUrl?count=5&offset=0"),
        PageLink("next", s"$baseUrl?count=5&offset=10")))
  }

  test("links for last page, including this") {
    assert(links(numberOfResults = Some(20), offset = 15, count = 5, baseUrl, includeSelf = true).toSet
      === Set(PageLink("this", s"$baseUrl?count=5&offset=15"),
        PageLink("prev", s"$baseUrl?count=5&offset=10")))
  }

  test("links for last page, excluding this") {
    assert(links(numberOfResults = Some(20), offset = 15, count = 5, baseUrl, includeSelf = false).toSet
      === Set(PageLink("prev", s"$baseUrl?count=5&offset=10")))
  }

  test("links when total number isn't known") {
    // Should always include the next link in this case.
    assert(links(numberOfResults = None, offset = 15, count = 5, baseUrl, includeSelf = true).toSet
      === Set(PageLink("this", s"$baseUrl?count=5&offset=15"),
        PageLink("prev", s"$baseUrl?count=5&offset=10"),
        PageLink("next", s"$baseUrl?count=5&offset=20")))

  }

}