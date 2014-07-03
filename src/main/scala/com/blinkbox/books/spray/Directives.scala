package com.blinkbox.books.spray

import spray.http.CacheDirectives._
import spray.http.DateTime
import spray.http.HttpHeaders._
import spray.httpx.marshalling.ToResponseMarshallable
import spray.routing.Directives._
import spray.routing.{Directive1, StandardRoute}

import scala.concurrent.duration.Duration

case class Page(offset: Int, count: Int) {
  require(offset >= 0, "Offset must be 0 or greater")
  require(count > 0, "Count must be greater than 0")
}

object SortOrder {
  val fieldParam = "order"
  val descParam = "desc"
}

case class SortOrder(field: String, desc: Boolean) {
  val asQueryParams = Seq((SortOrder.fieldParam, field), (SortOrder.descParam, desc))
}

trait Directives extends MonitoringDirectives {

  /**
   * Custom directive for extracting and validating page parameters (offset and count).
   * @param defaultCount The default number of results per page.
   */
  def paged(defaultCount: Int) = parameters('offset.as[Int] ? 0, 'count.as[Int] ? defaultCount).as(Page)

  /**
   * Custom directive for extracting and validating sort order parameters (order and desc).
   * @param defaultOrder The default sorting order.
   */
  def ordered(defaultOrder: SortOrder) = parameters(SortOrder.fieldParam ? defaultOrder.field, SortOrder.descParam.as[Boolean] ? defaultOrder.desc).as(SortOrder.apply _)

  /**
   * Custom directive for extracting and validating page and sort order parameters (offset, count, order, desc).
   * @param defaultOrder The default sort order.
   * @param defaultCount The default number of results per page.
   */
  def orderedAndPaged(defaultOrder: SortOrder, defaultCount: Int) = ordered(defaultOrder) & paged(defaultCount)

  /**
   * Directive for setting cache headers in a standard way.
   *
   * This sets time-dependent headers without requiring the use of a surrounding dynamic {} directive.
   *
   * @param maxAge The duration for which the returned value is cacheable.
   */
  def cacheable(maxAge: Duration) = provideTimeNow.flatMap { now => withCacheHeaders(now, maxAge) }

  /**
   * Directive for completing request while also setting cache headers for the result.
   *
   * @param maxAge The duration for which the returned value is cacheable.
   * @param response The response with which to complete the request.
   */
  def cacheable(maxAge: Duration, response: => ToResponseMarshallable): StandardRoute = cacheable(maxAge) & complete(response)

  /**
   * Directive for setting 'never cache' headers in a standard way.
   *
   * This sets time-dependent headers without requiring the use of a surrounding dynamic {} directive.
   */
  def neverCache = provideTimeNow flatMap { now => respondWithHeaders(
    `Cache-Control`(`no-store`),
    RawHeader("Expires", now.toRfc1123DateTimeString),
    RawHeader("Pragma", "no-cache"))
  }

  /**
   * Directive for completing request while also setting 'never cache' headers for the result.
   *
   * @param response The response with which to complete the request.
   */
  def uncacheable(response: => ToResponseMarshallable): StandardRoute = neverCache & complete(response)

  /** Get the current time, evaluated on each invocation of the directive. */
  private def provideTimeNow: Directive1[DateTime] = extract(_ => DateTime.now)

  /** Set cache headers. */
  private def withCacheHeaders(now: DateTime, maxAge: Duration) = respondWithHeaders(
    `Cache-Control`(`public`, `max-age`(maxAge.toSeconds)),
    RawHeader("Expires", (now + maxAge.toMillis).toRfc1123DateTimeString))
}

object Directives extends Directives
