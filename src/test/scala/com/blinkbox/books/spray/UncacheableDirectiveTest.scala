package com.blinkbox.books.spray

import com.blinkbox.books.spray.Directives._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import spray.http.StatusCodes._
import spray.routing.Directives._
import spray.testkit.ScalatestRouteTest
import scala.util.Try
import spray.http.DateTime

@RunWith(classOf[JUnitRunner])
class UncacheableDirectiveTest extends FunSuite with ScalatestRouteTest {

  test("The neverCache directive sets non-cacheable headers") {
    Get() ~> { neverCache { complete(OK) } } ~> check { checkNoCacheHeaders() }
  }

  test("The uncacheable directive sets non-cacheable headers") {
    Get() ~> { uncacheable(OK) } ~> check { checkNoCacheHeaders() }
  }

  test("The neverCache directive updates the Expires header dynamically") {
    Get() ~> { neverCache { complete(OK) } } ~> check {
      val time1 = parseDateTime(header("Expires").get.value).get
      Thread.sleep(1001)
      Get() ~> { neverCache { complete(OK) } } ~> check {
        assert(status == OK)
        val time2 = parseDateTime(header("Expires").get.value).get
        assert(time1 != time2, "Time field should be updated dynamically on each request")
      }
    }
  }

  test("The uncacheable directive updates the Expires header dynamically") {
    Get() ~> { uncacheable(OK) } ~> check {
      val time1 = parseDateTime(header("Expires").get.value).get
      Thread.sleep(1001)
      Get() ~> { uncacheable(OK) } ~> check {
        assert(status == OK)
        val time2 = parseDateTime(header("Expires").get.value).get
        assert(time1 != time2, "Time field should be updated dynamically on each request")
      }
    }
  }

  def checkNoCacheHeaders(): Unit = {
    assert(header("Cache-Control").get.value == "no-store")
    assert(parseDateTime(header("Expires").get.value).get <= DateTime.now)
    assert(header("Pragma").get.value == "no-cache")
  }

  // The expected date-time format.
  private val dateTimeFormatter = org.joda.time.format.DateTimeFormat.forPattern("E, d MMM yyyy HH:mm:ss z")

  def parseDateTime(str: String) = Try(DateTime(dateTimeFormatter.parseDateTime(str).getMillis))
}
