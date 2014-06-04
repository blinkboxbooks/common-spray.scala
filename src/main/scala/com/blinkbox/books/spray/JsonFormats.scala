package com.blinkbox.books.spray

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.json4s._
import org.json4s.JsonAST.{JNull, JString}
import spray.httpx.unmarshalling.{MalformedContent, FromStringDeserializer}
import scala.util.control.NonFatal

/**
 * Definitions for common data formats used in service requests and responses.
 */
object JsonFormats {

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
   * Deserializer to parse BigDecimal values in query parameters
   */
  implicit val BigDecimalDeserializer = new FromStringDeserializer[BigDecimal] {
    def apply(value: String) = {
      try Right(BigDecimal(value))
      catch {
        case NonFatal(ex) => Left(MalformedContent("'%s' is not a valid 128-bit BigDecimal value" format value, ex))
      }
    }
  }

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


