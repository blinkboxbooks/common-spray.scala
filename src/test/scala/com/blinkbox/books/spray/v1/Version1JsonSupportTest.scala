package com.blinkbox.books.spray.v1

import com.blinkbox.books.json.ExplicitTypeHints
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.http.HttpEntity
import spray.http.HttpCharsets._
import spray.httpx.marshalling._
import spray.httpx.unmarshalling._

case class Employee(fname: String, name: String, age: Int, id: Long, boardMember: Boolean) {
  require(!boardMember || age > 40, "Board members must be older than 40")
}

object Employee {
  val simple = Employee("Frank", "Smith", 42, 12345, boardMember = false)
  val json = """{"fname":"Frank","name":"Smith","age":42,"id":12345,"boardMember":false}"""
  val hintedJson = """{"type":"emp","fname":"Frank","name":"Smith","age":42,"id":12345,"boardMember":false}"""

  val utf8 = Employee("Fränk", "Smi√", 42, 12345, boardMember = false)
  val utf8json =
    """{
      |  "fname": "Fränk",
      |  "name": "Smi√",
      |  "age": 42,
      |  "id": 12345,
      |  "boardMember": false
      |}""".stripMargin.getBytes(`UTF-8`.nioCharset)

  val illegalEmployeeJson = """{"fname":"Little Boy","name":"Smith","age":7,"id":12345,"boardMember":true}"""
}

@RunWith(classOf[JUnitRunner])
class Version1JsonSupportTest extends FunSuite with Version1JsonSupport {

  test("Provide unmarshalling support for a case class") {
    assert(HttpEntity(`application/vnd.blinkboxbooks.data.v1+json`, Employee.json).as[Employee] == Right(Employee.simple))
  }

  test("Provide marshalling support for a case class") {
    assert(marshal(Employee.simple) == Right(HttpEntity(`application/vnd.blinkboxbooks.data.v1+json`.withCharset(`UTF-8`), Employee.json)))
  }

  test("Correctly decode UTF-8 characters") {
    assert(HttpEntity(`application/vnd.blinkboxbooks.data.v1+json`, Employee.utf8json).as[Employee] == Right(Employee.utf8))
  }

  test("provide proper error messages for requirement errors") {
    val Left(MalformedContent(msg, Some(_: IllegalArgumentException))) =
      HttpEntity(`application/vnd.blinkboxbooks.data.v1+json`, Employee.illegalEmployeeJson).as[Employee]
    assert(msg == "requirement failed: Board members must be older than 40")
  }

}

@RunWith(classOf[JUnitRunner])
class Version1JsonSupportResponseTypeHintsTest extends FunSuite with Version1JsonSupport {

  override val responseTypeHints = ExplicitTypeHints(Map(classOf[Employee] -> "emp"))

  test("Provide unmarshalling support for a case class without a type hint") {
    assert(HttpEntity(`application/vnd.blinkboxbooks.data.v1+json`, Employee.json).as[Employee] == Right(Employee.simple))
  }

  test("Provide unmarshalling support for a case class with a type hint") {
    assert(HttpEntity(`application/vnd.blinkboxbooks.data.v1+json`, Employee.hintedJson).as[Employee] == Right(Employee.simple))
  }

  test("Provide marshalling support with a type hint for a case class") {
    assert(marshal(Employee.simple) == Right(HttpEntity(`application/vnd.blinkboxbooks.data.v1+json`.withCharset(`UTF-8`), Employee.hintedJson)))
  }

}
