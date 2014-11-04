package com.blinkbox.books.spray.v2

import com.blinkbox.books.json.ExplicitTypeHints
import com.blinkbox.books.spray.v2
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.http.HttpCharsets._
import spray.http.{ContentType, HttpEntity}
import spray.httpx.marshalling.{CollectingMarshallingContext, marshal}
import spray.httpx.unmarshalling.{MalformedContent, PimpedHttpEntity}

case class Employee(fname: String, name: String, age: Int, id: Long, boardMember: Boolean, hiredDate: DateTime) {
  require(!boardMember || age > 40, "Board members must be older than 40")
}

case class DateBox(date: Option[DateTime])

object DateBox {
  val invalidDateJson = """{"date":"Ceci n'est pas un jour"}"""
}

object Employee {

  val hiredDate = DateTime.parse("2014-05-17T14:00:05Z")
  val simple = Employee("Frank", "Smith", 42, 12345, boardMember = false, hiredDate)
  val json = """{"fname":"Frank","name":"Smith","age":42,"id":12345,"boardMember":false,"hiredDate":"2014-05-17T14:00:05.000Z"}"""
  val hintedJson = """{"type":"emp","fname":"Frank","name":"Smith","age":42,"id":12345,"boardMember":false,"hiredDate":"2014-05-17T14:00:05.000Z"}"""

  val utf8 = Employee("Fränk", "Smi√", 42, 12345, boardMember = false, hiredDate)
  val utf8json =
    """{
      |  "fname": "Fränk",
      |  "name": "Smi√",
      |  "age": 42,
      |  "id": 12345,
      |  "boardMember": false,
      |  "hiredDate": "2014-05-17T14:00:05Z"
      |}""".stripMargin.getBytes(`UTF-8`.nioCharset)

  val illegalEmployeeJson = """{"fname":"Little Boy","name":"Smith","age":7,"id":12345,"boardMember":true,"hiredDate":"2014-05-17T14:00:05.000Z"}"""
  val badDateEmployeeJson = """{"fname":"John","name":"Smith","age":45,"id":12345,"boardMember":true,"hiredDate":"Ceci n'est pas un jour"}"""
}

@RunWith(classOf[JUnitRunner])
class JsonSupportTest extends FunSuite with v2.JsonSupport {

  override implicit def jsonFormats = JsonFormats.blinkboxFormat(throwOptionMappingExceptions = true)

  test("Provide unmarshalling support for a case class") {
    assert(HttpEntity(`application/vnd.blinkbox.books.v2+json`, Employee.json).as[Employee] == Right(Employee.simple))
  }

  test("Provide marshalling support for a case class") {
    assert(marshal(Employee.simple) == Right(HttpEntity(`application/vnd.blinkbox.books.v2+json`.withCharset(`UTF-8`), Employee.json)))
  }

  test("Correctly decode UTF-8 characters") {
    assert(HttpEntity(`application/vnd.blinkbox.books.v2+json`, Employee.utf8json).as[Employee] == Right(Employee.utf8))
  }

  test("provide proper error messages for requirement errors") {
    val Left(MalformedContent(msg, Some(_: IllegalArgumentException))) =
      HttpEntity(`application/vnd.blinkbox.books.v2+json`, Employee.illegalEmployeeJson).as[Employee]
    assert(msg == "requirement failed: Board members must be older than 40")
  }

  test("provide meaningful error messages for date time parse errors") {
    val Left(MalformedContent(msg, _)) = HttpEntity(`application/vnd.blinkbox.books.v2+json`, Employee.badDateEmployeeJson).as[Employee]
    assert(msg == "No usable value for hiredDate\n'Ceci n'est pas un jour' is not a valid ISO date")
  }

  test("throw a mapping exception when a value for an Option[T] can't be converted to the expected type") {
    val Left(MalformedContent(msg, _)) = HttpEntity(`application/vnd.blinkbox.books.v2+json`, DateBox.invalidDateJson).as[DateBox]
    assert(msg == "No usable value for date\n'Ceci n'est pas un jour' is not a valid ISO date")
  }
}

@RunWith(classOf[JUnitRunner])
class JsonSupportResponseTypeHintsTest extends FunSuite with v2.JsonSupport {

  override val responseTypeHints = ExplicitTypeHints(Map(classOf[Employee] -> "emp"))

  test("Provide unmarshalling support for a case class without a type hint") {
    assert(HttpEntity(`application/vnd.blinkbox.books.v2+json`, Employee.json).as[Employee] == Right(Employee.simple))
  }

  test("Provide unmarshalling support for a case class with a type hint") {
    assert(HttpEntity(`application/vnd.blinkbox.books.v2+json`, Employee.hintedJson).as[Employee] == Right(Employee.simple))
  }

  test("Provide marshalling support with a type hint for a case class") {
    assert(marshal(Employee.simple) == Right(HttpEntity(`application/vnd.blinkbox.books.v2+json`.withCharset(`UTF-8`), Employee.hintedJson)))
  }
}
