package com.blinkbox.books.spray

import java.lang.reflect.InvocationTargetException
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.json4s._
import org.json4s.JsonAST.{JNull, JString}
import org.json4s.jackson.Serialization
import spray.http._
import spray.httpx.unmarshalling.Unmarshaller
import spray.httpx.marshalling.Marshaller

/**
 * Definitions for common data formats used in service requests and responses.
 */
object JsonFormats {

  /**
   * Common media type.
   */
  val `application/vnd.blinkboxbooks.data.v1+json` = MediaTypes.register(MediaType.custom(
    mainType = "application",
    subType = "vnd.blinkboxbooks.data.v1+json",
    binary = true, // binary as the encoding is defined as utf-8 by the json spec
    compressible = true))

  /**
   * Class that allows custom strings to be used as type hints for classes, by passing in a map
   * of classes to the strings that they should be tagged with in generated JSON.
   */
  case class ExplicitTypeHints(customHints: Map[Class[_], String]) extends TypeHints {
    private val hintToClass = customHints.map(_.swap)
    override val hints = customHints.keys.toList
    override def hintFor(clazz: Class[_]) = customHints.get(clazz).get
    override def classFor(hint: String) = hintToClass.get(hint)
  }

  /**
   * Serializer for Joda DateTime objects in the ISO date format. This ensures that time zone
   * information is not lost.
   */
  case object ISODateTimeSerializer extends CustomSerializer[DateTime](_ => ({
    case JString(s) => ISODateTimeFormat.dateTime().parseDateTime(s)
    case JNull => null
  }, {
    case d: DateTime => JString(ISODateTimeFormat.dateTime().print(d))
  }))

  /**
   * Custom JSON format that performs JSON serialisation in a standard way.
   *
   * This format names the type hint field as "type".
   *
   * @param hints   The object that decides what type fields, if any, to insert into generated JSON,
   * and for deserialising objects from polymorphic lists. By default, no type hints will be used.
   */
  def blinkboxFormat(hints: TypeHints = NoTypeHints): Formats = new DefaultFormats {
    override val typeHints: TypeHints = hints
    override val typeHintFieldName: String = "type"
    override val wantsBigDecimal: Boolean = true // for endpoints that deal with money
  } + ISODateTimeSerializer

}

import JsonFormats._

/**
 * Configures the JSON support for the blinkbox books v1 media type.
 */
trait Version1JsonSupport {
  implicit def version1JsonFormats: Formats = blinkboxFormat()

  implicit def version1JsonUnmarshaller[T: Manifest] =
    Unmarshaller[T](`application/vnd.blinkboxbooks.data.v1+json`) {
      case x: HttpEntity.NonEmpty =>
        try Serialization.read[T](x.asString(defaultCharset = HttpCharsets.`UTF-8`))
        catch {
          case MappingException("unknown error", ite: InvocationTargetException) => throw ite.getCause
        }
    }

  implicit def version1JsonMarshaller[T <: AnyRef] =
    Marshaller.delegate[T, String](`application/vnd.blinkboxbooks.data.v1+json`)(Serialization.write(_))
}
