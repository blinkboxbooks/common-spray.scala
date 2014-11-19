package com.blinkbox.books.spray.v2

import com.blinkbox.books.json.DefaultFormats
import org.json4s.jackson.Serialization
import spray.httpx.marshalling.Marshaller

object Implicits {
  /**
   * Throwable marshaller for V2 API responses
   */
  implicit def throwableMarshaller: Marshaller[Throwable] =
    Marshaller.delegate[Throwable, String](`application/vnd.blinkbox.books.v2+json`) { t =>
      Serialization.write(Error(t))(DefaultFormats)
    }
}
