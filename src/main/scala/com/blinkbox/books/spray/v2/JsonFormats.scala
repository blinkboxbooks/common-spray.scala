package com.blinkbox.books.spray.v2

import com.blinkbox.books.json.DefaultFormats
import org.json4s.{Formats, NoTypeHints, TypeHints}

/**
 * Definitions for common data formats used in service requests and responses.
 */
object JsonFormats {

  /**
   * Custom JSON format that performs JSON serialisation in a standard way.
   *
   * This format names the type hint field as "type".
   *
   * @param hints   The object that decides what type fields, if any, to insert into generated JSON,
   * and for deserialising objects from polymorphic lists. By default, no type hints will be used.
   * @param throwOptionMappingExceptions throws a MappingException when a value for key of class
   * Option[T] value can't be can't be converted to the expected type.
   */
  def blinkboxFormat(hints: TypeHints = NoTypeHints, throwOptionMappingExceptions: Boolean = false): Formats = new DefaultFormats {
    override val typeHints: TypeHints = hints
    override val typeHintFieldName: String = "type"
    override val strict = throwOptionMappingExceptions
  }

}