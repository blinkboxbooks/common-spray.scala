package com.blinkbox.books.spray.v2

import com.blinkbox.books.json.DefaultFormats
import org.json4s.jackson.Serialization
import spray.http.HttpEntity
import spray.httpx.marshalling.Marshaller

object Implicits {
  /**
   * Error marshaller for V2 API responses
   */
  implicit def errorMarshallerForV2API: Marshaller[Error] =
    Marshaller.delegate[Error, String](`application/vnd.blinkbox.books.v2+json`)(Serialization.write(_)(DefaultFormats))

  /**
   * Error marshaller for V1 API responses
   */
  implicit def errorMarshallerForV1API: Marshaller[Error] = Marshaller[Error] { (_, ctx) => ctx.marshalTo(HttpEntity.Empty) }
}
