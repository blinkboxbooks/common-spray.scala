package com.blinkbox.books.spray

import com.blinkbox.books.json.DefaultFormats
import org.joda.time.format.{DateTimeFormatterBuilder, ISODateTimeFormat}
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s._
import spray.httpx.unmarshalling.{FromStringDeserializer, MalformedContent}

import scala.util.control.NonFatal

/**
 * Definitions for common data formats used in service requests and responses.
 */
object JsonFormats {

  /**
   * Deserializer to parse BigDecimal values in query parameters
   */
  implicit val BigDecimalDeserializer = new FromStringDeserializer[BigDecimal] {
    def apply(value: String) =
      try Right(BigDecimal(value))
      catch {
        case NonFatal(ex) => Left(MalformedContent(s"'$value' is not a valid 128-bit BigDecimal value", ex))
      }
  }

  /**
   * Deserializer to parse Joda DateTime values in query parameters
   */
  implicit val ISODateTimeDeserializer = new FromStringDeserializer[DateTime] {
    val parsers = Array(ISODateTimeFormat.dateTime.getParser, ISODateTimeFormat.dateTimeNoMillis.getParser)
    val format = new DateTimeFormatterBuilder().append(null, parsers).toFormatter
    def apply(value: String) =
      try Right(format.parseDateTime(value).withZone(DateTimeZone.UTC))
      catch {
        case NonFatal(ex) => Left(MalformedContent(s"'$value' is not a valid ISO date format value", ex))
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
  }

}
