package com.blinkbox.books.spray.v2

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

case class BarBox(bar: String)
trait TestRoute extends HttpService with JsonSupport {
  val route = get {
    path("barbox" / IntNumber) { num =>
      val boxes = for (i <- 1 to num) yield BarBox(s"foobar$i")
      complete(ListPage(boxes.toList, lastPage = num > 10))
    } ~ path ("barbox") {
      complete(ListPage(List.empty[BarBox], lastPage = false))
    }
  }
}

@RunWith(classOf[JUnitRunner])
class ListPageTest extends FunSuite with ScalatestRouteTest with TestRoute {

  override val actorRefFactory = system
  val expectedPage = ListPage(items = List(BarBox("foobar")), lastPage = true)
  val expectedEmptyPage = ListPage(items = List(), lastPage = false)

  val foobarBoxString = """{"items":[{"bar":"foobar1"}],"lastPage":false}"""
  val emptyBoxString = """{"items":[],"lastPage":false}"""

  test("can construct a ListPage") {
    assert(ListPage(List(BarBox("foobar")), lastPage = true) == expectedPage)
  }

  test("can construct a ListPage with an empty list") {
    assert(ListPage(List(), lastPage = false) == expectedEmptyPage)
  }

  test("ListPage serializes to JSON (one item)") {
    Get("/barbox/1") ~> route ~> check {
      assert(foobarBoxString == response.entity.asString)
      assert(responseAs[ListPage[BarBox]] == ListPage(List(BarBox("foobar1")), lastPage = false))
    }
  }

  test("ListPage serializes to JSON (multiple items, lastPage = false)") {
    Get("/barbox/5") ~> route ~> check {
      val expectedBarBoxes = for (i <- 1 to 5) yield BarBox(s"foobar$i")
      assert(responseAs[ListPage[BarBox]] == ListPage(expectedBarBoxes.toList, lastPage = false))
    }
  }

  test("ListPage serializes to JSON (multiple items, lastPage = true)") {
    Get("/barbox/11") ~> route ~> check {
      val expectedBarBoxes = for (i <- 1 to 11) yield BarBox(s"foobar$i")
      assert(responseAs[ListPage[BarBox]] == ListPage(expectedBarBoxes.toList, lastPage = true))
    }
  }

  test("ListPage serializes to JSON (empty item list, lastPage = false)") {
    Get("/barbox") ~> route ~> check {
      assert(emptyBoxString == response.entity.asString)
      assert(responseAs[ListPage[BarBox]] == ListPage(List.empty[BarBox], lastPage = false))
    }
  }

}
