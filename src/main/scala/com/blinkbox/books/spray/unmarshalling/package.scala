package com.blinkbox.books.spray

import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.{DateTimeFormatterBuilder, ISODateTimeFormat}
import spray.httpx.unmarshalling._
import scala.util.control.NonFatal

package object unmarshalling {

  /**
   * Deserializer to parse BigDecimal values in query parameters.
   */
  implicit val BigDecimalDeserializer = new FromStringDeserializer[BigDecimal] {
    def apply(value: String) =
      try Right(BigDecimal(value))
      catch {
        case NonFatal(ex) => Left(MalformedContent(s"'$value' is not a valid decimal value", ex))
      }
  }

  /**
   * Deserializer to parse Joda DateTime values in query parameters.
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

}
