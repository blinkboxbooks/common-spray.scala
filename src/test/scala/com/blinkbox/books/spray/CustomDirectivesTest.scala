package com.blinkbox.books.spray

import com.blinkbox.books.spray.v1.SortOrder
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes._
import spray.routing.{HttpService, Route}
import spray.testkit.ScalatestRouteTest

trait CustomDirectivesService extends HttpService with Directives {

  val defaultCount = 42
  var receivedPage: Page = _

  val defaultOrder = SortOrder("NAME", desc = true)
  var receivedOrder: SortOrder = _

  def route: Route = {
    get {
      path("pagedEndpoint") {
        paged(defaultCount) { page =>
          receivedPage = page
          complete(OK)
        }
      } ~
      path("sortedEndpoint") {
        sorted(defaultOrder) { sortOrder =>
          receivedOrder = sortOrder
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

  test("Sorting with default parameters") {
    Get("/sortedEndpoint") ~> route ~> check {
      assert(status === OK)
      assert(receivedOrder === SortOrder("NAME", desc = true))
    }
  }

  test("Sorting with parameters in request") {
    Get("/sortedEndpoint?order=COUNTRY&desc=false") ~> route ~> check {
      assert(status === OK)
      assert(receivedOrder === SortOrder("COUNTRY", desc = false))
    }
  }

  test("Sorting with invalid sort ordering") {
    Get("/sortedEndPoint?order=COUNTRY") ~> route ~> check {
      assert(!handled)
    }
  }
}
