package com.blinkbox.books

import _root_.spray.http.RemoteAddress.IP
import _root_.spray.http.{HttpHeader, HttpRequest, RemoteAddress, Uri}
import _root_.spray.http.HttpHeaders.{`Remote-Address`, `X-Forwarded-For`}
import java.net.{InetAddress, URI, URL}
import com.blinkbox.books.spray.Paging.PageLink
import com.blinkbox.books.spray.v1.Link

import scala.language.implicitConversions

package object spray {
  implicit def uri2uri(uri: URI) = Uri(uri.toString)
  implicit def url2uri(url: URL) = Uri(url.toString)

  implicit def pageLink2Link(pageLink: PageLink) = Link(pageLink.rel, pageLink.href, None, None)

  /**
   * Adds functionality to a `HttpRequest`.
   * @param request The request.
   */
  implicit class RichHttpRequest(request: HttpRequest) {

    /**
     * Attempts to get the client IP address for the request.
     *
     * This method uses logic based on that used in Ruby's Rack server to try and obtain the first external
     * address, based on the assumption that internal addresses won't try to spoof the IP address. It's not
     * foolproof but it's probably good enough.
     *
     * @return The client IP, if available.
     */
    def clientIP: Option[RemoteAddress] = {
      def isExternal(addr: RemoteAddress) = addr match {
        case IP(ip) => !ip.isLoopbackAddress && !ip.isAnyLocalAddress && !ip.isSiteLocalAddress
        case _ => false
      }
      val forwardedFor = request.header[`X-Forwarded-For`].flatMap { header =>
        header.addresses.filter(isExternal).lastOption.orElse(header.addresses.headOption)
      }
      forwardedFor.orElse(request.header[`Remote-Address`].map(_.address))
    }
  }
}
