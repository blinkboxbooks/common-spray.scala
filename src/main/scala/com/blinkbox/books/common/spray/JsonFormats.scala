package com.blinkbox.books.common.spray

import spray.http.MediaTypes
import spray.http.MediaType
import org.json4s.Formats
import org.json4s.NoTypeHints
import org.json4s.TypeHints
import org.json4s.DefaultFormats

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
