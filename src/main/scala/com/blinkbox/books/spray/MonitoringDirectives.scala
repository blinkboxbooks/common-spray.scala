package com.blinkbox.books.spray

import org.slf4j.{Logger, MDC}
import scala.util.control.NonFatal
import spray.http.{RequestProcessingException, IllegalRequestException}
import spray.http.StatusCodes._
import spray.routing.{Directive0, ExceptionHandler, RejectionHandler}
import spray.routing.directives.{BasicDirectives, ExecutionDirectives}

/**
 * Provides directives for monitoring the runtime behaviour of services.
 */
trait MonitoringDirectives {
  import BasicDirectives._
  import ExecutionDirectives._

  /**
   * A magnet to bind to an SLF4J `Logger` using implicit conversions.
   *
   * It may not be obvious why we're using a regular `Logger` instead of the standard spray `LoggingContext`.
   * This is because we want to use MDC, and the MDC implementation in Akka logging (on which `LoggingContext`
   * depends) is very tightly bound to Akka's concept of the current message. This concept falls down when
   * trying to work at a higher level with spray routing directives where it isn't possible to override the
   * `mdc` method in any reasonable manner as you tend to be acting on behaviour triggered by the message
   * rather than on properties of the message itself.
   *
   * It's not ideal by any means, but unfortunately logging with MDC lives on the boundary where high
   * level abstractions of asynchronous work meet the harsh reality of machine threads and this seems
   * to be the pragmatic solution in Spray.
   *
   * @param log The logger.
   */
  class LoggerMagnet(val log: Logger)

  object LoggerMagnet {
    import scala.language.implicitConversions
    implicit def fromLogger(log: Logger) = new LoggerMagnet(log)
    implicit def fromUnit(u: Unit)(implicit log: Logger) = new LoggerMagnet(log)
  }

  /**
   * Provides information to monitoring systems about the request and response.
   *
   * This method uses magnets, but the simplified signatures are:
   *
   * {{{
   * def monitor(log: Logger): Directive0
   * def monitor()(implicit log: Logger): Directive0
   * }}}
   *
   * Because this directive uses MDC for logging contextual information, the actor hosting the
   * routes should extend `DiagnosticActorLogging` which ensures that MDC is available and that
   * it is reset correctly around the receive of each message.
   *
   * To ensure that the response is logged correctly this directive will convert any rejections
   * or exceptions into an `HttpResponse` using the default `RejectionHandler` and `ExceptionHandler`
   * implementations, meaning that no subsequent processing of rejections or exceptions can occur.
   * This shouldn't be a problem as this directive should be the outermost one after `runRoute` when
   * constructing routes.
   *
   * An example of usage is below. Note that the brackets are required after the directive even
   * when using an implicit logger to prevent the inner route being interpreted as an attempt to
   * explicitly provide a logger.
   *
   * {{{
   *  class MyHttpService extends HttpServiceActor with DiagnosticActorLogging {
   *    implicit val log = LoggerFactory.getLogger(classOf[MyHttpService])
   *
   *    def receive = runRoute {
   *      monitor() {
   *        complete(OK) // do something more useful here
   *      }
   *    }
   *  }
   * }}}
   */
  def monitor(magnet: LoggerMagnet): Directive0 =
    logRequestResponseDetails(magnet.log) &
    handleExceptions(monitorExceptionHandler(magnet.log)) &
    handleRejections(RejectionHandler.Default)

  // this directive is built around mapRequestContext/withHttpResponseMapped to ensure that the
  // timing information is correct. if you use logRequestResponse you don't get the 'before'
  // hook, and if you use mapInnerRoute which might seem like the more obvious choice then you
  // don't necessarily get the 'after' hook running at the right time as it seems that spray can
  // sometimes optimise the transformation and run it before the response actually completes.
  private def logRequestResponseDetails(log: Logger): Directive0 = mapRequestContext { ctx =>
    val request = ctx.request
    MDC.put("httpMethod", request.method.name)
    MDC.put("httpPath", request.uri.path.toString())
    MDC.put("httpPathAndQuery", request.uri.toRelative.toString())
    MDC.put("httpClientIP", request.clientIP.getOrElse("").toString)
    val timestamp = System.currentTimeMillis
    ctx withHttpResponseMapped { response =>
      val duration = System.currentTimeMillis - timestamp
      MDC.put("httpStatus", response.status.intValue.toString)
      MDC.put("httpDuration", duration.toString)
      val message = s"${request.method} ${request.uri.path} returned ${response.status} in ${duration}ms"
      response.status match {
        case ServerError(_) => log.error(message)
        case ClientError(_) => log.warn(message)
        case _ => log.info(message)
      }
      response
    }
  }

  // an exception handler based on the default exception handler logic, but which uses the standard
  // logger rather than LoggingContext so that the MDC information is logged with the error.
  private def monitorExceptionHandler(log: Logger) = ExceptionHandler {
    case e: IllegalRequestException => ctx =>
      log.warn("Illegal request", e)
      ctx.complete(e.status, Option.empty[String])
    case e: RequestProcessingException => ctx =>
      log.error("Failed to process request", e)
      ctx.complete(e.status, Option.empty[String])
    case NonFatal(e) => ctx =>
      log.error("Unexpected error processing request", e)
      ctx.complete(InternalServerError, Option.empty[String])
  }
}

object MonitoringDirectives extends MonitoringDirectives
