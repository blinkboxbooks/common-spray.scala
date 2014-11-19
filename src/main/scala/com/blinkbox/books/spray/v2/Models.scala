package com.blinkbox.books.spray.v2

import java.net.URI

import spray.http.StatusCodes._
import spray.http.{RequestProcessingException, IllegalRequestException}

import scala.util.control.NonFatal

case class ListPage[T] (
  items: List[T],
  lastPage: Boolean
)

trait Relation

case class Link(rel: Relation, url: URI)

case class Error(code: String, developerMessage: Option[String])

object Error {
  def apply(t: Throwable): Error = t match {
    case e: IllegalRequestException => Error(e.status.reason.replace(" ", ""), Some(e.status.defaultMessage))
    case e: RequestProcessingException => Error(e.status.reason.replace(" ", ""), Some(e.status.defaultMessage))
    case NonFatal(e) => Error("InternalServerError", Some(InternalServerError.defaultMessage))
  }
 }