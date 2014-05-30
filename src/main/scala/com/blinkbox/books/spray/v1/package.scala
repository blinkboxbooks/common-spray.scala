package com.blinkbox.books.spray

import spray.http.{MediaType, MediaTypes}

/**
 * Provides support for the version 1 media type, including common model objects.
 */
package object v1 {

  /**
   * The version 1 media type.
   */
  val `application/vnd.blinkboxbooks.data.v1+json` = MediaTypes.register(MediaType.custom(
    mainType = "application",
    subType = "vnd.blinkboxbooks.data.v1+json",
    binary = true, // binary as the encoding is defined as utf-8 by the json spec
    compressible = true))

}
