package com.blinkbox.books

import _root_.spray.http.Uri
import java.net.{URI, URL}
import scala.language.implicitConversions

package object spray {
  implicit def uri2uri(uri: URI) = Uri(uri.toString)
  implicit def url2uri(url: URL) = Uri(url.toString)
}
