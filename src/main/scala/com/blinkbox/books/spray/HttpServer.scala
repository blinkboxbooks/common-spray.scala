package com.blinkbox.books.spray

import akka.actor.ActorSystem
import akka.io.{Tcp, IO}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import spray.can.Http

import scala.concurrent.ExecutionContext
import scala.util.Success

object HttpServer extends StrictLogging {
  def apply(bind: Http.Bind)(implicit system: ActorSystem, executionContext: ExecutionContext, timeout: Timeout): Unit =
    IO(Http) ? bind onComplete {
      case Success(Http.Bound(endpoint)) =>
        logger.info(s"HTTP interface bound to $endpoint")
      case Success(Tcp.CommandFailed(Http.Bind(_, endpoint, _, _, _))) =>
        logger.error(s"HTTP bind to $endpoint failed")
        sys.exit(1)
      case x =>
        logger.error(s"Unexpected HTTP bind result: $x")
        sys.exit(2)
    }
}