package com.blinkbox.books.spray

import org.slf4j.{Logger, MDC}
import spray.http.StatusCodes.{ClientError, ServerError}
import spray.routing.{Directive0, ExceptionHandler, RejectionHandler}
import spray.routing.directives.{BasicDirectives, ExecutionDirectives}

/**
 * Provides directives for monitoring the runtime behaviour of services.
 */
trait MonitoringDirectives {
  import BasicDirectives._
  import ExecutionDirectives._

  /**
   * A magnet to bind to a [[org.slf4j.Logger]] using implicit conversions.
   *
   * It may not be obvious why we're not using the standard [[spray.util.LoggingContext]] but instead
   * a regular `Logger`. This is because we want to use MDC, and the MDC implementation in Akka logging
   * (on which `LoggingContext` depends) is very tightly bound to Akka's concept of the current message.
   * This concept falls down when trying to work at a higher level with spray routing directives where
   * it isn't possible to override the `mdc` method in any reasonable manner as you tend to be acting on
   * behaviour triggered by the message rather than on properties of the message itself.
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
   * into an `HttpResponse` using the implicitly available `RejectionHandler`, meaning that no
   * subsequent processing of rejections can occur. This shouldn't be a problem as this directive
   * should be the outermost one after `runRoute` when constructing routes.
   *
   * Note that if the route terminates by throwing an exception then it will not be logged by this
   * directive. To ensure that all exception responses are correctly returned you should use the
   * `handleExceptions` directive immediately inside this one with your chosen exception handler.
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
   *        handleExceptions(ExceptionHandler.default) {
   *          complete(OK) // do something more useful here
   *        }
   *      }
   *    }
   *  }
   * }}}
   */
  def monitor(magnet: LoggerMagnet): Directive0 = mapRequestContext { ctx =>
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
        case ServerError(_) => magnet.log.error(message)
        case ClientError(_) => magnet.log.warn(message)
        case _ => magnet.log.debug(message)
      }
      response
    }
  } & handleRejections(implicitly[RejectionHandler])
}

object MonitoringDirectives extends MonitoringDirectives
