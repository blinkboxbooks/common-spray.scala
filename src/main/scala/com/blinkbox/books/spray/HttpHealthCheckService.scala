package com.blinkbox.books.spray

import com.blinkbox.books.logging.DiagnosticExecutionContext
import com.codahale.metrics.json.HealthCheckModule
import com.codahale.metrics.health.{HealthCheck, HealthCheckRegistry}
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck
import java.util.concurrent.ExecutorService
import org.json4s.jackson.JsonMethods._
import shapeless.{HList, HNil}
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.http.Uri
import spray.routing._
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext
import spray.http.Uri.Path

private object HealthCheckHttpService {
  mapper.registerModule(new HealthCheckModule())
  implicit class RichResults(val results: java.util.Map[String, HealthCheck.Result]) extends AnyVal {
    def isHealthy = results.forall { case (_, result) => result.isHealthy }
  }
}

trait HealthCheckHttpService extends HttpService with Directives {
  import HealthCheckHttpService._

  implicit val executionContext: ExecutionContext = DiagnosticExecutionContext(actorRefFactory.dispatcher)
  val basePath: Uri.Path

  final val healthChecks = {
    val registry = new HealthCheckRegistry
    registry.register("deadlocks", new ThreadDeadlockHealthCheck)
    registry
  }

  lazy val routes: Route = get {
    rootPath(basePath) {
      path("health" / "report") {
        parameter('pretty.as[Boolean] ? false) { pretty =>
          dynamic {
            detach() {
              val results = executionContext match {
                case es: ExecutorService => healthChecks.runHealthChecks(es)
                case _ => healthChecks.runHealthChecks()
              }
              val status = if (results.isEmpty) NotImplemented else if (results.isHealthy) OK else InternalServerError
              val writer = if (pretty) mapper.writerWithDefaultPrettyPrinter else mapper.writer
              val json = writer.writeValueAsString(results)
              respondWithMediaType(`application/json`) {
                uncacheable(status, json.toString)
              }
            }
          }
        }
      }
    }
  }

}