package com.blinkbox.books.spray

import akka.actor.ActorSystem
import com.codahale.metrics.health.HealthCheck
import org.json4s.JsonAST.{JString, JBool}
import org.json4s.jackson.JsonMethods._
import org.mockito.Mockito.when
import org.scalatest.{Matchers, FunSuite}
import org.scalatest.mock.MockitoSugar
import spray.http.CacheDirectives._
import spray.http.HttpHeaders._
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.http.Uri.Path
import spray.testkit.ScalatestRouteTest

class HealthCheckHttpServiceTests extends FunSuite with ScalatestRouteTest with MockitoSugar with Matchers {

  test("Ping returns the word 'pong' as text/plain content") {
    val service = basicHealthCheckService()
    Get("/health/ping") ~> service.routes ~> check {
      assert(contentType.mediaType == `text/plain`)
      assert(body.asString == "pong")
    }
  }

  test("Ping does not allow the response to be cached") {
    val service = basicHealthCheckService()
    Get("/health/ping") ~> service.routes ~> check {
      assert(header[`Cache-Control`] == Some(`Cache-Control`(`no-store`)))
    }
  }

  test("Health report returns application/json content") {
    val service = basicHealthCheckService()
    Get("/health/report") ~> service.routes ~> check {
      assert(contentType.mediaType == `application/json`)
    }
  }

  test("Health report does not allow the response to be cached") {
    val service = basicHealthCheckService()
    Get("/health/report") ~> service.routes ~> check {
      assert(header[`Cache-Control`] == Some(`Cache-Control`(`no-store`)))
    }
  }

  test("Health report returns healthy status for a passed check") {
    val healthCheck = mock[HealthCheck]
    when(healthCheck.execute()).thenReturn(HealthCheck.Result.healthy("A message!"))
    val service = basicHealthCheckService()
    service.healthChecks.register("good", healthCheck)
    Get("/health/report") ~> service.routes ~> check {
      assert(status == OK)
      val json = parse(body.asString)
      assert((json \\ "good" \\ "healthy") == JBool(true))
      assert((json \\ "good" \\ "message") == JString("A message!"))
    }
  }

  test("Health report returns unhealthy status for a failed check") {
    val healthCheck = mock[HealthCheck]
    when(healthCheck.execute()).thenReturn(HealthCheck.Result.unhealthy("A sad message :-("))
    val service = basicHealthCheckService()
    service.healthChecks.register("bad", healthCheck)
    Get("/health/report") ~> service.routes ~> check {
      assert(status == InternalServerError)
      val json = parse(body.asString)
      assert((json \\ "bad" \\ "healthy") == JBool(false))
      assert((json \\ "bad" \\ "message") == JString("A sad message :-("))
    }
  }

  test("Health report can be mounted at non-root URLs") {
    val service = basicHealthCheckService("/some/root")
    Get("/some/root/health/report") ~> service.routes ~> check {
      assert(contentType.mediaType == `application/json`)
    }
  }

  test("Thread dump returns a thread dump as text/plain content") {
    val service = basicHealthCheckService()
    Get("/health/threads") ~> service.routes ~> check {
      assert(contentType.mediaType == `text/plain`)
      assert(body.asString.length > 0) // not sure how else to check this content!
    }
  }

  test("Thread dump does not allow the response to be cached") {
    val service = basicHealthCheckService()
    Get("/health/threads") ~> service.routes ~> check {
      assert(header[`Cache-Control`] == Some(`Cache-Control`(`no-store`)))
    }
  }

  private def basicHealthCheckService(root: String = "/") =
    new HealthCheckHttpService {
      override implicit def actorRefFactory = ActorSystem("test")
      override val basePath = Path(root)
    }
}
