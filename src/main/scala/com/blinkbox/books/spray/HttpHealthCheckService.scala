package com.blinkbox.books.spray

import java.io.ByteArrayOutputStream
import java.lang.management.ManagementFactory
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

import com.blinkbox.books.logging.DiagnosticExecutionContext
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck
import com.codahale.metrics.health.{HealthCheck, HealthCheckRegistry}
import com.codahale.metrics.json.HealthCheckModule
import com.codahale.metrics.jvm.ThreadDump
import org.json4s.jackson.JsonMethods._
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.http.Uri
import spray.routing._

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext

private object HealthCheckHttpService {
  mapper.registerModule(new HealthCheckModule())

  private val healthExecutionContext = DiagnosticExecutionContext(ExecutionContext.fromExecutorService(Executors.newCachedThreadPool))

  private val threadDump =
    try Some(new ThreadDump(ManagementFactory.getThreadMXBean))
    catch { case _: NoClassDefFoundError => None }

  implicit class RichResults(val results: java.util.Map[String, HealthCheck.Result]) extends AnyVal {
    def isHealthy = results.forall { case (_, result) => result.isHealthy }
  }
}

trait HealthCheckHttpService extends HttpService with Directives {
  import HealthCheckHttpService._

  val basePath: Uri.Path

  final val healthChecks = {
    val registry = new HealthCheckRegistry
    registry.register("deadlocks", new ThreadDeadlockHealthCheck)
    registry
  }

  lazy val routes: Route = get {
    rootPath(basePath) {
      path("health" / "ping") {
        uncacheable(OK, "pong")
      } ~
      path("health" / "report") {
        parameter('pretty.as[Boolean] ? false) { pretty =>
          dynamic {
            detach(healthExecutionContext) {
              // TODO: If DiagnosticExecutionContext supported the ExecutorService interface then we could
              // run the health checks in parallel by passing it to the runHealthChecks method. We probably
              // should get around to implementing that, but it hasn't been done yet.
              val results = healthChecks.runHealthChecks()
              val status = if (results.isEmpty) NotImplemented else if (results.isHealthy) OK else InternalServerError
              val writer = if (pretty) mapper.writerWithDefaultPrettyPrinter else mapper.writer
              val json = writer.writeValueAsString(results)
              respondWithMediaType(`application/json`) {
                uncacheable(status, json.toString)
              }
            }
          }
        }
      } ~
      path("health" / "threads") {
        dynamic {
          detach(healthExecutionContext) {
            threadDump match {
              case Some(dumper) =>
                val output = new ByteArrayOutputStream
                dumper.dump(output)
                uncacheable(OK, new String(output.toByteArray, StandardCharsets.UTF_8))
              case None =>
                uncacheable(NotImplemented, "The runtime environment does not allow threads to be dumped.")
            }
          }
        }
      }
    }
  }

}