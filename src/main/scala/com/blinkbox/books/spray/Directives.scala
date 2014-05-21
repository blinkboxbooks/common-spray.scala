package com.blinkbox.books.spray

import spray.routing.HttpService

import JsonFormats._

case class Page(offset: Int, count: Int) {
  require(offset >= 0, "Offset must be 0 or greater")
  require(count > 0, "Count must be greater than 0")
}

trait Directives {
  this: HttpService =>

  /**
   * Custom directive for extracting and validating page parameters (offset and count).
   */
  def paged(defaultCount: Int) = parameters('offset.as[Int] ? 0, 'count.as[Int] ? defaultCount).as(Page)
}
