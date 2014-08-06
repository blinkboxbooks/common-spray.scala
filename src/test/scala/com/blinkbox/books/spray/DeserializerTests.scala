package com.blinkbox.books.spray

import com.blinkbox.books.spray.v1.Version1JsonSupport
import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.ISODateTimeFormat
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuite, PartialFunctionValues}
import spray.httpx.unmarshalling._
import spray.routing.{HttpService, MalformedQueryParamRejection, Rejection, Route}
import spray.testkit.ScalatestRouteTest
import JsonFormats._

trait TestService extends HttpService with Directives with Version1JsonSupport {
  def route: Route = {
    get {
      path("bigdecimal") {
        parameter('amount.as[BigDecimal])(complete(_))
      } ~
      path("datetime") {
        parameter('value.as[DateTime])(complete(_))
      }
    }
  }
}

@RunWith(classOf[JUnitRunner])
class DeserializerTests extends FunSuite with ScalatestRouteTest with PartialFunctionValues with TestService {
  override implicit def actorRefFactory = system

  test("deserialises BigDecimal from query parameter") {
    Get("/bigdecimal?amount=123.0001") ~> route ~> check {
      assert(responseAs[BigDecimal] == BigDecimal("123.0001"))
    }
  }

  test("BigDecimal deserialiser returns 128 bit values") {
    Get("/bigdecimal?amount=123.000000000000000000000000000000001") ~> route ~> check {
      assert(responseAs[BigDecimal] == BigDecimal("123.00000000000000000000000000000000"))
    }
  }

  test("BigDecimal deserialiser rejects invalid value with correct error message") {
    val pf: PartialFunction[Rejection, Boolean] = {
      case MalformedQueryParamRejection("amount", "'23453f' is not a valid 128-bit BigDecimal value", Some(_)) => true
    }

    Get("/bigdecimal?amount=23453f") ~> route ~> check {
      assert(pf.valueAt(rejection) == true)
    }
  }

  test("deserialises DateTime without milliseconds in UTC time zone from query parameter") {
    Get(s"/datetime?value=2014-05-17T14:00:05Z") ~> route ~> check {
      assert(responseAs[DateTime] == new DateTime(2014, 5, 17, 14, 0, 5, DateTimeZone.UTC))
    }
  }

  test("deserialises DateTime without milliseconds in non-UTC time zone from query parameter as UTC") {
    Get(s"/datetime?value=2014-05-17T14:00:05%2B03:00") ~> route ~> check {
      assert(responseAs[DateTime] == new DateTime(2014, 5, 17, 11, 0, 5, DateTimeZone.UTC))
    }
  }

  test("deserialises DateTime with milliseconds in UTC time zone from query parameter") {
    Get(s"/datetime?value=2014-05-17T14:00:05.367Z") ~> route ~> check {
      assert(responseAs[DateTime] == new DateTime(2014, 5, 17, 14, 0, 5, 367, DateTimeZone.UTC))
    }
  }

  test("deserialises DateTime with milliseconds in non-UTC time zone from query parameter as UTC") {
    Get(s"/datetime?value=2014-05-17T14:00:05.367%2B03:00") ~> route ~> check {
      assert(responseAs[DateTime] == new DateTime(2014, 5, 17, 11, 0, 5, 367, DateTimeZone.UTC))
    }
  }

  test("DateTime deserialiser rejects invalid value with correct error message") {
    val pf: PartialFunction[Rejection, Boolean] = {
      case MalformedQueryParamRejection("value", "'2014-05-17T14:00:05' is not a valid ISO date format value", Some(_)) => true
    }

    Get(s"/datetime?value=2014-05-17T14:00:05") ~> route ~> check {
      assert(pf.valueAt(rejection) == true)
    }
  }
}