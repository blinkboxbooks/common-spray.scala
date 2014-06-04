package com.blinkbox.books.spray

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.{PartialFunctionValues, FunSuite}
import spray.testkit.ScalatestRouteTest
import spray.routing.{Rejection, MalformedQueryParamRejection, Route, HttpService}
import spray.httpx.unmarshalling._
import com.blinkbox.books.spray.v1.Version1JsonSupport
import JsonFormats.BigDecimalDeserializer


trait TestService extends HttpService with Directives with Version1JsonSupport {

  def route: Route = get {
    parameter('amount.as[BigDecimal]) { a => complete(a)}
  }
}

@RunWith(classOf[JUnitRunner])
class BigDecimalDeserializerTests extends FunSuite with ScalatestRouteTest with PartialFunctionValues with TestService {

  override implicit def actorRefFactory = system

  test("deserialises BigDecimal from query parameter") {
    Get("/?amount=123.0001") ~> route ~> check {
      assert(responseAs[BigDecimal] === BigDecimal("123.0001"))
    }
  }

  test("BigDecimal deserializer returns 128 bit values") {
    Get("/?amount=123.000000000000000000000000000000001") ~> route ~> check {
      assert(responseAs[BigDecimal] === BigDecimal("123.00000000000000000000000000000000"))
    }
  }

  test("BigDecimal deserializer rejects invalid value with correct error message") {

    val pf: PartialFunction[Rejection, Boolean] = { case  MalformedQueryParamRejection("amount", "'23453f' is not a valid 128-bit BigDecimal value", Some(_)) => true }

    Get("/?amount=23453f") ~> route ~> check {
      assert(pf.valueAt(rejection) === true)
    }
  }
}