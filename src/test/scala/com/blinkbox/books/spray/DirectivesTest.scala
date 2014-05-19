package com.blinkbox.books.spray

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes._
import spray.routing.{ ExceptionHandler, HttpService, HttpServiceActor, Route }
import spray.testkit.ScalatestRouteTest
import akka.actor.Actor
import spray.routing.RoutingSettings
import shapeless.get

/**
 * A trait that contains a route using the 'paged' directive.
 */
trait PagedService extends HttpService with Directives {

  val defaultCount = 42
  var receivedPage: Page = _

  def route: Route = get {
    path("pagedEndpoint") {
      paged(defaultCount) { page =>
        receivedPage = page
        complete(OK)
      }
    }
  }
}

@RunWith(classOf[JUnitRunner])
class DirectivesTest extends FunSuite with ScalatestRouteTest with PagedService {

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

}
