package com.blinkbox.books.spray.v1

import java.lang.reflect.InvocationTargetException
import org.json4s.{MappingException, NoTypeHints, TypeHints, Formats}
import org.json4s.jackson.Serialization
import spray.http.{HttpCharsets, HttpEntity}
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller

import com.blinkbox.books.spray.JsonFormats._

/**
 * Configures the JSON support for the version 1 media type.
 */
trait Version1JsonSupport {
  implicit def version1JsonFormats: Formats = blinkboxFormat()

  /**
   * Provides type hints that are used only when marshalling responses, but ignored for requests. This
   * allows clients to send entity representations without a type hint.
   */
  def responseTypeHints: TypeHints = NoTypeHints

  implicit def version1JsonUnmarshaller[T: Manifest] =
    Unmarshaller[T](`application/vnd.blinkboxbooks.data.v1+json`) {
      case x: HttpEntity.NonEmpty =>
        try Serialization.read[T](x.asString(defaultCharset = HttpCharsets.`UTF-8`))
        catch {
          case MappingException("unknown error", ite: InvocationTargetException) => throw ite.getCause
        }
    }

  implicit def version1JsonMarshaller[T <: AnyRef] = {
    val formats = implicitly[Formats] + responseTypeHints
    Marshaller.delegate[T, String](`application/vnd.blinkboxbooks.data.v1+json`)(Serialization.write(_)(formats))
  }
}
