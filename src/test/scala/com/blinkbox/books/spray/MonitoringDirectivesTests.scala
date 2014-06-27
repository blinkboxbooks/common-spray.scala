package com.blinkbox.books.spray

import java.util.concurrent.atomic.AtomicReference
import org.mockito.invocation.InvocationOnMock
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.Answer
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import org.slf4j.{MDC, Logger}
import spray.http.HttpChallenge
import spray.http.HttpHeaders.{`X-Forwarded-For`, `WWW-Authenticate`, RawHeader, `Remote-Address`}
import spray.http.StatusCodes._
import spray.routing.AuthenticationFailedRejection
import spray.routing.AuthenticationFailedRejection.CredentialsMissing
import spray.routing.Directives._
import spray.testkit.ScalatestRouteTest

class MonitoringDirectivesTests extends FunSuite with ScalatestRouteTest with MockitoSugar with MonitoringDirectives {

  test("monitor logs a debug message with the HTTP endpoint, status and duration for successful requests") {
    val messageRef = new AtomicReference[String]()
    implicit val log = mock[Logger]
    when(log.debug(any(classOf[String]))).thenAnswer(new Answer[Unit] {
      override def answer(invocation: InvocationOnMock): Unit = {
        messageRef.set(invocation.getArguments.head.asInstanceOf[String])
      }
    })

    Get("/path?q=1") ~> { monitor() { complete(OK) } } ~> check {
      assert(messageRef.get() matches "GET /path returned 200 OK in [0-9]+ms")
    }
  }

  test("monitor logs a warning message with the HTTP endpoint, status and duration for client errors") {
    val messageRef = new AtomicReference[String]()
    implicit val log = mock[Logger]
    when(log.warn(any(classOf[String]))).thenAnswer(new Answer[Unit] {
      override def answer(invocation: InvocationOnMock): Unit = {
        messageRef.set(invocation.getArguments.head.asInstanceOf[String])
      }
    })

    val rejection = AuthenticationFailedRejection(CredentialsMissing, `WWW-Authenticate`(HttpChallenge(scheme = "http", realm = "test")) :: Nil)
    Get("/path?q=1") ~> { monitor() { reject(rejection) } } ~> check {
      assert(messageRef.get() matches "GET /path returned 401 Unauthorized in [0-9]+ms")
    }
  }

  test("monitor logs an error message with the HTTP endpoint, status and duration for server errors") {
    val messageRef = new AtomicReference[String]()
    implicit val log = mock[Logger]
    when(log.error(any(classOf[String]))).thenAnswer(new Answer[Unit] {
      override def answer(invocation: InvocationOnMock): Unit = {
        messageRef.set(invocation.getArguments.head.asInstanceOf[String])
      }
    })

    Get("/path?q=1") ~> { monitor() { complete(InternalServerError) } } ~> check {
      assert(messageRef.get() matches "GET /path returned 500 Internal Server Error in [0-9]+ms")
    }
  }

  test("monitor logs an error message with the HTTP endpoint, status and duration when an exception is thrown") {
    val messageRef = new AtomicReference[String]()
    implicit val log = mock[Logger]
    when(log.error(any(classOf[String]))).thenAnswer(new Answer[Unit] {
      override def answer(invocation: InvocationOnMock): Unit = {
        messageRef.set(invocation.getArguments.head.asInstanceOf[String])
      }
    })

    Get("/path?q=1") ~> { monitor() { dynamic { throw new Exception("o noes!") } } } ~> check {
      assert(messageRef.get() matches "GET /path returned 500 Internal Server Error in [0-9]+ms")
    }
  }

  test("monitor adds key HTTP properties to the MDC context") {
    val mdcRef = new AtomicReference[java.util.Map[_, _]]()
    implicit val log = mock[Logger]
    when(log.debug(any(classOf[String]))).thenAnswer(new Answer[Unit] {
      override def answer(invocation: InvocationOnMock): Unit = {
        mdcRef.set(MDC.getCopyOfContextMap)
      }
    })

    Get("/path?q=1") ~> { monitor() { complete(OK) } } ~> check {
      val mdc = mdcRef.get()
      assert(mdc.get("httpMethod") == "GET")
      assert(mdc.get("httpPath") == "/path")
      assert(mdc.get("httpPathAndQuery") == "/path?q=1")
      assert(mdc.get("httpStatus") == "200")
      assert(mdc.get("httpDuration").toString matches "[0-9]+")
    }
  }

  test("monitor adds the client IP if the Remote-Address header is valid") {
    val mdcRef = new AtomicReference[java.util.Map[_, _]]()
    implicit val log = mock[Logger]
    when(log.debug(any(classOf[String]))).thenAnswer(new Answer[Unit] {
      override def answer(invocation: InvocationOnMock): Unit = {
        mdcRef.set(MDC.getCopyOfContextMap)
      }
    })

    Get("/path?q=1") ~> `Remote-Address`("192.168.0.1") ~> { monitor() { complete(OK) } } ~> check {
      val mdc = mdcRef.get()
      assert(mdc.get("httpClientIP") == "192.168.0.1")
    }
  }

  test("monitor does not cause the request to fail if the Remote-Address header is invalid") {
    val mdcRef = new AtomicReference[java.util.Map[_, _]]()
    implicit val log = mock[Logger]
    when(log.debug(any(classOf[String]))).thenAnswer(new Answer[Unit] {
      override def answer(invocation: InvocationOnMock): Unit = {
        mdcRef.set(MDC.getCopyOfContextMap)
      }
    })

    Get("/path?q=1") ~> RawHeader("Remote-Address", "invalid") ~> { monitor() { complete(OK) } } ~> check {
      val mdc = mdcRef.get()
      assert(mdc.get("httpStatus") == "200")
    }
  }

  test("monitor adds the client IP if the X-Forwarded-For header is valid") {
    val mdcRef = new AtomicReference[java.util.Map[_, _]]()
    implicit val log = mock[Logger]
    when(log.debug(any(classOf[String]))).thenAnswer(new Answer[Unit] {
      override def answer(invocation: InvocationOnMock): Unit = {
        mdcRef.set(MDC.getCopyOfContextMap)
      }
    })

    Get("/path?q=1") ~> `X-Forwarded-For`("192.168.1.1", "192.168.1.2") ~> { monitor() { complete(OK) } } ~> check {
      val mdc = mdcRef.get()
      assert(mdc.get("httpClientIP") == "192.168.1.1")
    }
  }

  test("monitor does not cause the request to fail if the X-Forwarded-For header is invalid") {
    val mdcRef = new AtomicReference[java.util.Map[_, _]]()
    implicit val log = mock[Logger]
    when(log.debug(any(classOf[String]))).thenAnswer(new Answer[Unit] {
      override def answer(invocation: InvocationOnMock): Unit = {
        mdcRef.set(MDC.getCopyOfContextMap)
      }
    })

    Get("/path?q=1") ~> RawHeader("X-Forwarded-For", "invalid") ~> { monitor() { complete(OK) } } ~> check {
      val mdc = mdcRef.get()
      assert(mdc.get("httpStatus") == "200")
    }
  }

}
