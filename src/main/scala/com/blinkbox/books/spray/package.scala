package com.blinkbox.books

import _root_.spray.http.RemoteAddress.IP
import _root_.spray.http.{HttpRequest, RemoteAddress, Uri}
import _root_.spray.http.HttpHeaders.{`Remote-Address`, `X-Forwarded-For`}
import java.net.{InetAddress, URI, URL}
import scala.language.implicitConversions

package object spray {
  implicit def uri2uri(uri: URI) = Uri(uri.toString)
  implicit def url2uri(url: URL) = Uri(url.toString)

  implicit class RichHttpRequest(request: HttpRequest) {
    def clientIP: Option[RemoteAddress] =
      request.header[`Remote-Address`].flatMap {
        case `Remote-Address`(address) => Some(address)
        case _ => None
      }.orElse(request.header[`X-Forwarded-For`].flatMap {
        case `X-Forwarded-For`(Seq(address, _*)) => Some(address)
        case _ => None
      })
  }
}
