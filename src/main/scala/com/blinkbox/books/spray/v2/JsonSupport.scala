package com.blinkbox.books.spray.v2

import java.lang.reflect.InvocationTargetException

import com.blinkbox.books.spray.v2.JsonFormats._
import org.json4s.jackson.Serialization
import org.json4s.{Formats, MappingException, NoTypeHints, TypeHints}
import spray.http.{HttpCharsets, HttpEntity}
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller

/**
 * Configures the JSON support for the version 2 media type.
 */
trait JsonSupport {
  implicit def jsonFormats: Formats = blinkboxFormat()

  /**
   * Provides type hints that are used only when marshalling responses, but ignored for requests. This
   * allows clients to send entity representations without a type hint.
   */
  def responseTypeHints: TypeHints = NoTypeHints

  implicit def jsonUnmarshaller[T: Manifest] =
    Unmarshaller[T](`application/vnd.blinkbox.books.v2+json`) {
      case x: HttpEntity.NonEmpty =>
        try Serialization.read[T](x.asString(defaultCharset = HttpCharsets.`UTF-8`))
        catch {
          case MappingException("unknown error", ite: InvocationTargetException) => throw ite.getCause
        }
    }

  implicit def jsonMarshaller[T <: AnyRef] = {
    val formats = implicitly[Formats] + responseTypeHints
    Marshaller.delegate[T, String](`application/vnd.blinkbox.books.v2+json`)(Serialization.write(_)(formats))
  }
}
