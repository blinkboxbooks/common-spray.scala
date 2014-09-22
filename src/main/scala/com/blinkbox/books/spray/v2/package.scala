package com.blinkbox.books.spray

import spray.http.{MediaType, MediaTypes}

package object v2 {

  /**
   * The version 2 media type.
   */
  val `application/vnd.blinkbox.books.v2+json` = MediaTypes.register(MediaType.custom(
    mainType = "application",
    subType = "vnd.blinkbox.books.v2+json",
    binary = true, // binary as the encoding is defined as utf-8 by the json spec
    compressible = true))
}