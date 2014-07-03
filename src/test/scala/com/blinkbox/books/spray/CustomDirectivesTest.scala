package com.blinkbox.books.spray

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes._
import spray.routing.{HttpService, Route}
import spray.testkit.ScalatestRouteTest

trait CustomDirectivesService extends HttpService with Directives {

  val defaultCount = 42
  val defaultOrder = SortOrder("NAME", desc = true)

  var receivedPage: Page = _
  var receivedOrder: SortOrder = _

  def route: Route = {
    get {
      path("pagedEndpoint") {
        paged(defaultCount) { page =>
          receivedPage = page
          complete(OK)
        }
      } ~
      path("orderedEndpoint") {
        ordered(defaultOrder) { sortOrder =>
          receivedOrder = sortOrder
          complete(OK)
        }
      } ~
      path("orderedAndPagedEndpoint") {
        orderedAndPaged(defaultOrder, defaultCount) { (sortOrder, page) =>
          receivedOrder = sortOrder
          receivedPage = page
          complete(OK)
        }
      }
    }
  }
}

@RunWith(classOf[JUnitRunner])
class DirectivesTest extends FunSuite with ScalatestRouteTest with CustomDirectivesService {

  override implicit def actorRefFactory = system

  test("Paging with default parameters") {
    Get("/pagedEndpoint") ~> route ~> check {
      assert(status === OK)
      assert(receivedPage === Page(offset = 0, count = defaultCount))
    }
  }

  test("Paging with parameters in request") {
    Get("/pagedEndpoint?offset=5&count=42") ~> route ~> check {
      assert(status === OK)
      assert(receivedPage === Page(offset = 5, count = 42))
    }
  }

  test("Try to request invalid offset") {
    Get("/pagedEndpoint?offset=5x") ~> route ~> check {
      assert(!handled)
    }
  }

  test("Try to request invalid count") {
    Get("/pagedEndpoint?count=abc") ~> route ~> check {
      assert(!handled)
    }
  }

  test("Ordering with default parameters") {
    Get("/orderedEndpoint") ~> route ~> check {
      assert(status === OK)
      assert(receivedOrder === defaultOrder)
    }
  }

  test("Ordering with parameters in request") {
    Get("/orderedEndpoint?order=COUNTRY&desc=false") ~> route ~> check {
      assert(status === OK)
      assert(receivedOrder === SortOrder("COUNTRY", desc = false))
    }
  }

  test("Ordering with invalid sort ordering") {
    Get("/orderedEndpoint?order=COUNTRY&desc=whatever") ~> route ~> check {
      assert(!handled)
    }
  }

  test("Ordering and Paging with default parameters") {
    Get("/orderedAndPagedEndpoint") ~> route ~> check {
      assert(status === OK)
      assert(receivedPage === Page(offset = 0, count = defaultCount))
      assert(receivedOrder === defaultOrder)
    }
  }
}
