package com.blinkbox.books.spray

import org.slf4j.{Logger, MDC}
import spray.http.StatusCodes.{ClientError, ServerError}
import spray.routing.{ExceptionHandler, Directive0, RejectionHandler}
import spray.routing.directives.{BasicDirectives, ExecutionDirectives}

/**
 * Provides diagnostic and debugging directives.
 */
trait DebuggingDirectives {
  import BasicDirectives._
  import ExecutionDirectives._

  class LoggerMagnet(val log: Logger)

  object LoggerMagnet {
    import scala.language.implicitConversions
    implicit def fromLogger(log: Logger) = new LoggerMagnet(log)
    implicit def fromUnit(u: Unit)(implicit log: Logger) = new LoggerMagnet(log)
  }

  /**
   * Provides diagnostics information to monitoring systems about the request and response.
   *
   * To ensure that the response is logged correctly this directive will convert any rejections
   * into an `HttpResponse` using the implicitly available `RejectionHandler`, meaning that no
   * subsequent processing of rejections can occur. This shouldn't be a problem as this directive
   * should be the outermost one after `runRoute` when constructing routes.
   *
   * Note that if the route terminates by throwing an exception then it will not be logged by this
   * directive. To ensure that all exception responses are correctly returned you should use the
   * `handleExceptions` directive inside this one, if it is possible for exceptions to be thrown
   * during the processing of the route.
   *
   * This method uses magnets, but the simplified signatures are:
   *
   * {{{
   * def diagnostics(log: Logger): Directive0
   * def diagnostics()(implicit log: Logger): Directive0
   * }}}
   *
   * An example of usage is below. Note that the brackets are required after the directive even
   * when using an implicit logger to prevent the inner route being interpreted as an attempt to
   * explicitly provide a logger.
   *
   * {{{
   * class MyHttpService extends HttpServiceActor {
   *  implicit val log = LoggerFactory.getLogger(classOf[MyHttpService])
   *
   *  def receive = runRoute {
   *    diagnostics() {
   *      complete(OK)
   *    }
   *  }
   * }
   * }}}
   */
  def diagnostics(magnet: LoggerMagnet): Directive0 = mapRequestContext { ctx =>
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

object DebuggingDirectives extends DebuggingDirectives
