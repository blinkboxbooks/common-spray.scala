package com.blinkbox.books.spray

import org.json4s.jackson.Serialization.{read, write}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import JsonFormats._

// Some classes to test JSON serialisation with.
trait Thing
case class Widget(stringValue: String) extends Thing
case class Gizmo(intValue: Int) extends Thing
case class Things(things: List[Thing])

@RunWith(classOf[JUnitRunner])
class JsonFormatsTest extends FunSuite {

  val things = Things(List(Widget("I'm a widget"), Gizmo(42)))

  test("format without type hints") {
    // Use the default format, which doesn't use type hints.
    implicit val formats = blinkboxFormat()

    val serialised = write(things)
    assert(serialised === """{"things":[{"stringValue":"I'm a widget"},{"intValue":42}]}""")

    // Shouldn't be able to deserialise a list with mixed types without type hints.
    intercept[Exception] { read[Things](serialised) }
  }

  test("format with type hints") {
    // Specify some explicit hints for types.
    val typeHints = ExplicitTypeHints(Map(classOf[Gizmo] -> "urn:gizmo", classOf[Widget] -> "urn:widget"))
    implicit val formats = blinkboxFormat(typeHints)

    val serialised = write(things)
    assert(serialised === """{"things":[{"type":"urn:widget","stringValue":"I'm a widget"},{"type":"urn:gizmo","intValue":42}]}""")
    val deserialised = read[Things](serialised)
    assert(deserialised === things, "Should come out the same after serialisation + deserialisation")
  }

  test("deserialise class that doesn't have a defined type hint") {
    // Define a type hint for one of the classes only.
    val typeHints = ExplicitTypeHints(Map(classOf[Gizmo] -> "urn:gizmo"))
    implicit val formats = blinkboxFormat(typeHints)

    val data = List(Widget("I'm a widget"))
    val deserialised = read[List[Widget]](write(data))

    assert(data === deserialised, "Should be able to deserialise object with no defined type hint")
  }
}
