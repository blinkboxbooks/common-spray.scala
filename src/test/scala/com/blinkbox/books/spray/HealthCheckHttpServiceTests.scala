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

  test("Returns application/json content") {
    val service = new HealthCheckHttpService {
      override implicit def actorRefFactory = ActorSystem("test")
      override val basePath = Path("/")
    }
    Get("/health/report") ~> service.routes ~> check {
      assert(contentType.mediaType == `application/json`)
    }
  }

  test("Does not allow the response to be cached") {
    val service = new HealthCheckHttpService {
      override implicit def actorRefFactory = ActorSystem("test")
      override val basePath = Path("/")
    }
    Get("/health/report") ~> service.routes ~> check {
      assert(header[`Cache-Control`] == Some(`Cache-Control`(`no-store`)))
    }
  }

  test("Returns healthy status for a passed check") {
    val healthCheck = mock[HealthCheck]
    when(healthCheck.execute()).thenReturn(HealthCheck.Result.healthy("A message!"))
    val service = new HealthCheckHttpService {
      healthChecks.register("good", healthCheck)
      override implicit def actorRefFactory = ActorSystem("test")
      override val basePath = Path("/")
    }
    Get("/health/report") ~> service.routes ~> check {
      assert(status == OK)
      val json = parse(body.asString)
      assert((json \\ "good" \\ "healthy") == JBool(true))
      assert((json \\ "good" \\ "message") == JString("A message!"))
    }
  }

  test("Returns unhealthy status for a failed check") {
    val healthCheck = mock[HealthCheck]
    when(healthCheck.execute()).thenReturn(HealthCheck.Result.unhealthy("A sad message :-("))
    val service = new HealthCheckHttpService {
      healthChecks.register("bad", healthCheck)
      override implicit def actorRefFactory = ActorSystem("test")
      override val basePath = Path("/")
    }
    Get("/health/report") ~> service.routes ~> check {
      assert(status == InternalServerError)
      val json = parse(body.asString)
      assert((json \\ "bad" \\ "healthy") == JBool(false))
      assert((json \\ "bad" \\ "message") == JString("A sad message :-("))
    }
  }

  test("Can be mounted at non-root URLs") {
    val service = new HealthCheckHttpService {
      override implicit def actorRefFactory = ActorSystem("test")
      override val basePath = Path("/some/root")
    }
    Get("/some/root/health/report") ~> service.routes ~> check {
      assert(contentType.mediaType == `application/json`)
    }
  }
  
}
