package com.blinkbox.books.spray

import com.blinkbox.books.json.DefaultFormats
import org.json4s._

/**
 * Definitions for common data formats used in service requests and responses.
 */
object JsonFormats {

  @deprecated("Use com.blinkbox.books.spray.unmarshalling.BigDecimalSerializer instead", "0.16.0")
  implicit val BigDecimalDeserializer = unmarshalling.BigDecimalDeserializer

  @deprecated("Use com.blinkbox.books.spray.unmarshalling.ISODateTimeDeserializer instead", "0.16.0")
  implicit val ISODateTimeDeserializer = unmarshalling.ISODateTimeDeserializer

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
