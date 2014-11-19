package com.blinkbox.books.spray.v1

import spray.http.HttpEntity
import spray.httpx.marshalling.Marshaller

object Implicits {
  /**
   * Throwable marshaller for V1 API responses
   */
  implicit def throwableMarshaller: Marshaller[Throwable] = Marshaller[Throwable] { (_, ctx) => ctx.marshalTo(HttpEntity.Empty) }
}
