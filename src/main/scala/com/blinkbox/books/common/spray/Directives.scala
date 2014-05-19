package com.blinkbox.books.common.spray

import org.json4s.Formats
import org.json4s.TypeHints
import org.json4s.NoTypeHints
import org.json4s.DefaultFormats
import spray.http.HttpEntity
import spray.http.MediaType
import spray.http.MediaTypes
import spray.routing.HttpService

import JsonFormats._

/**
 * Values for a requested page.
 */
case class Page(offset: Int, count: Int) {
  require(offset >= 0, "Offset must be 0 or greater")
  require(count > 0, "Count must be greater than 0")
}

trait Directives {

  this: HttpService =>

  /**
   * Directive that removes the character set encoding from content types, typically "; UTF-8".
   */
  def removeCharsetEncoding(entity: HttpEntity) =
    entity.flatMap(e => HttpEntity(e.contentType.withoutDefinedCharset, e.data))

  /**
   * Combination of all response directives that are usually applied to service endpoints.
   * Details TBD!
   */
  val standardResponseHeaders = mapHttpResponseEntity(removeCharsetEncoding) &
    respondWithMediaType(`application/vnd.blinkboxbooks.data.v1+json`)

  /**
   * Custom directive for extracting and validating page parameters (offset and count).
   */
  def paged(defaultCount: Int) = parameters('offset.as[Int] ? 0, 'count.as[Int] ? defaultCount).as(Page)

}
