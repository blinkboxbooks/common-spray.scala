package com.blinkbox.books.spray.v2

import com.blinkbox.books.json.DefaultFormats
import org.json4s.jackson.Serialization
import spray.http.StatusCodes._
import spray.http.{RequestProcessingException, IllegalRequestException}
import spray.httpx.marshalling.Marshaller

import scala.util.control.NonFatal

object Implicits {
  /**
   * Throwable marshaller for V2 API responses
   */
  implicit def throwableMarshaller: Marshaller[Throwable] =
    Marshaller.delegate[Throwable, String](`application/vnd.blinkbox.books.v2+json`) { t =>
      val error = t match {
        case e: IllegalRequestException => Error(e.status.reason.replace(" ", ""), Some(e.status.defaultMessage))
        case e: RequestProcessingException => Error(e.status.reason.replace(" ", ""), Some(e.status.defaultMessage))
        case NonFatal(e) => Error("InternalServerError", Some(InternalServerError.defaultMessage))
      }
      Serialization.write(error)(DefaultFormats)
    }
}
