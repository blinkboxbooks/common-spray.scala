package com.blinkbox.books.spray

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scala.concurrent.duration._
import scala.util.Try
import spray.http.DateTime
import spray.http.StatusCodes._
import spray.routing.{ HttpService, Route }
import spray.testkit.ScalatestRouteTest

/**
 * A trait that contains a route using the 'cacheable' directive.
 */
private[spray] trait CacheableService extends HttpService with Directives {

  val maxAge = 1.minute

  // Test route.
  // This is defined as a val to check that headers are set dynamically even with a static route.
  val route: Route = get {
    path("cacheableEndpoint") {
      cacheable(maxAge) {
        complete(OK)
      }
    } ~
      path("nonCacheableEndpoint") {
        complete(OK)
      } ~
      path("cacheableCompletedEndpoint") {
        cacheable(maxAge, OK)
      }
  }
}

@RunWith(classOf[JUnitRunner])
class CacheableDirectiveTest extends FunSuite with ScalatestRouteTest with CacheableService {

  override implicit def actorRefFactory = system

  test("Response headers on cacheable endpoint") {
    val start = DateTime.now
    Get("/cacheableEndpoint") ~> route ~> check {
      assert(status == OK)
      assert(header("Cache-Control").get.value == s"public, max-age=${maxAge.toSeconds}")

      val expiryTimeStr = header("Expires").get.value
      assert(parseDateTime(expiryTimeStr).isSuccess, "Should have set expiry time in the valid format")
    }
  }

  test("Response headers when completing cacheable object") {
    val start = DateTime.now
    Get("/cacheableCompletedEndpoint") ~> route ~> check {
      assert(status == OK)
      assert(header("Cache-Control").get.value == s"public, max-age=${maxAge.toSeconds}")

      val expiryTimeStr = header("Expires").get.value
      assert(parseDateTime(expiryTimeStr).isSuccess, "Should have set expiry time in the valid format")
    }
  }

  test("Response headers are updated dynamically") {
    Get("/cacheableEndpoint") ~> route ~> check {
      val time1 = parseDateTime(header("Expires").get.value).get

      // Sleep for a second, to ensure date header will be updated.
      Thread.sleep(1001)
      Get("/cacheableEndpoint") ~> route ~> check {
        assert(status == OK)
        val time2 = parseDateTime(header("Expires").get.value).get
        assert(time1 != time2, "Time field should be updated dynamically on each request")
      }
    }
  }

  test("No cache related response headers on non-cacheable endpoint") {
    Get("/nonCacheableEndpoint") ~> route ~> check {
      assert(status == OK)
      assert(header("Cache-Control") == None)
      assert(header("Expires") == None)
    }
  }

  // The expected date-time format.
  private val dateTimeFormatter = org.joda.time.format.DateTimeFormat.forPattern("E, d MMM yyyy HH:mm:ss z")

  def parseDateTime(str: String) = Try(DateTime(dateTimeFormatter.parseDateTime(str).getMillis()))

}
