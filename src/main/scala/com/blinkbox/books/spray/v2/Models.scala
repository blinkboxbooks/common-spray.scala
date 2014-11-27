package com.blinkbox.books.spray.v2

import java.net.URI
import java.util.concurrent.RejectedExecutionException

import spray.http.StatusCodes._
import spray.http.{StatusCode, RequestProcessingException, IllegalRequestException}

import scala.util.control.NonFatal

case class ListPage[T] (
  items: List[T],
  lastPage: Boolean
)

trait Relation

case class Link(rel: Relation, url: URI)

case class Error(code: String, developerMessage: Option[String])

object Error {
  def apply(status: StatusCode): Error = Error(status.reason.replace(" ", ""), Some(status.defaultMessage))
  def apply(t: Throwable): Error = t match {
    case e: IllegalRequestException => Error(e.status)
    case e: RequestProcessingException => Error(e.status)
    case e: RejectedExecutionException => Error(ServiceUnavailable)
    case NonFatal(e) => Error(InternalServerError)
  }
}