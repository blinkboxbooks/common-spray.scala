package com.blinkbox.books.spray.v2

import com.blinkbox.books.spray.v2
import org.scalatest.FlatSpec
import spray.http.HttpHeaders._
import spray.http.MediaTypes.{`application/pdf`, `text/css`, `text/xml`}
import spray.http.StatusCodes._
import spray.http._
import spray.httpx.encoding.{Deflate, Gzip}
import spray.httpx.unmarshalling.Unmarshaller
import spray.routing.Directives.{entity => extractEntity, _}
import spray.routing.authentication.BasicAuth
import spray.testkit.ScalatestRouteTest

import scala.xml.NodeSeq

class RejectionHandlerTests extends FlatSpec with ScalatestRouteTest with v2.JsonSupport {

  val wrap = handleRejections(RejectionHandler.V2ErrorRejectionHandler)
  val completeOk = complete(HttpResponse())

  "Rejection Handler" should "respond with Error for requests resulting in Nil" in {
    Get() ~> wrap { reject } ~> check {
      assert(status == NotFound)
      assert(responseAs[Error] == Error("NotFound", Some("The requested resource could not be found but may be available again in the future.")))
    }
  }

  it should "respond with Unauthorized for requests resulting in an AuthenticationFailedRejection" in {
    Get() ~> Authorization(BasicHttpCredentials("bob", "")) ~> wrap {
      authenticate(BasicAuth()) { _ => completeOk }
    } ~> check {
      assert(status == Unauthorized)
      assert(responseAs[Error] == Error("Unauthorized", Some("The supplied authentication is invalid")))
    }
  }

  it should "respond with Unauthorized plus WWW-Authenticate header for AuthenticationRequiredRejections" in {
    Get() ~> wrap {
      authenticate(BasicAuth()) { _ => completeOk }
    } ~> check {
      assert(status == Unauthorized)
      assert(headers == `WWW-Authenticate`(HttpChallenge("Basic", "Secured Resource")) :: Nil)

      assert(responseAs[Error] == Error("Unauthorized", Some("The resource requires authentication, which was not supplied with the request")))
    }
  }

  it should "respond with Forbidden for requests resulting in an AuthorizationFailedRejection" in {
    Get() ~> wrap {
      authorize(check = false) { completeOk }
    } ~> check {
      assert(status == Forbidden)
      assert(responseAs[Error] == Error("Forbidden", Some("The supplied authentication is not authorized to access this resource")))
    }
  }

  it should "respond with BadRequest for requests resulting in a CorruptRequestEncodingRejection" in {
    Get("/", "xyz") ~> `Content-Encoding`(HttpEncodings.gzip) ~> wrap {
      decodeRequest(Gzip) { completeOk }
    } ~> check {
      assert(status == BadRequest)
      assert(responseAs[Error] == Error("BadRequest", Some("The requests encoding is corrupt:\nNot in GZIP format")))
    }
  }

  it should "respond with BadRequest for requests resulting in a MalformedFormFieldRejection" in {
    import spray.httpx.marshalling.BasicMarshallers.FormDataMarshaller
    import spray.httpx.unmarshalling.FormDataUnmarshallers._
    Post("/", FormData(Map("amount" -> "12.2"))) ~> wrap {
      formField('amount.as[Int]) { _ ⇒ completeOk }
    } ~> check {
      assert(status == BadRequest)
      assert(responseAs[Error] == Error("BadRequest", Some("The form field 'amount' was malformed:\n'12.2' is not a valid 32-bit integer value")))
    }
  }

  it should "respond with BadRequest for requests resulting in a MalformedQueryParamRejection" in {
    Post("/?amount=xyz") ~> wrap {
      parameters('amount.as[Int]) { _ ⇒ completeOk }
    } ~> check {
      assert(status == BadRequest)
      assert(responseAs[Error] == Error("BadRequest", Some("The query parameter 'amount' was malformed:\n'xyz' is not a valid 32-bit integer value")))
    }
  }

  it should "respond with BadRequest for requests resulting in MalformedRequestContentRejections" in {
    import org.scalatest.OptionValues._
    Post("/", HttpEntity(`text/xml`, "<broken>xmlbroken>")) ~> wrap {
      extractEntity(as[NodeSeq]) { _ ⇒ completeOk }
    } ~> check {
      assert(status == BadRequest)
      val error = responseAs[Error]
      assert(error.code == "BadRequest")
      assert(error.developerMessage.value.startsWith("The request content was malformed:"))
    }
  }

  it should "respond with MethodNotAllowed for requests resulting in MethodRejections" in {
    import spray.http.HttpMethods._
    Post("/", "/test") ~> wrap {
      get { complete("yes") } ~
        put { complete("yes") }
    } ~> check {
      assert(status == MethodNotAllowed)
      assert(headers == Allow(GET, PUT) :: Nil)
      assert(responseAs[Error] == Error("MethodNotAllowed", Some("HTTP method not allowed, supported methods: GET, PUT")))
    }
  }

  it should "respond with BadRequest for requests resulting in SchemeRejections" in {
    Get("http://example.com/hello") ~> wrap {
      scheme("https") { complete("yes") }
    } ~> check {
      assert(status == BadRequest)
      assert(responseAs[Error] == Error("BadRequest", Some("Uri scheme not allowed, supported schemes: https")))
    }
  }

  it should "respond with BadRequest for requests resulting in a MissingFormFieldRejection" in {
    Get() ~> wrap {
      formFields('amount, 'orderId) { (_, _) ⇒ completeOk }
    } ~> check {
      assert(status == BadRequest)
      assert(responseAs[Error] == Error("BadRequest", Some("Request is missing required form field 'amount'")))
    }
  }

  it should "respond with NotFound for requests resulting in a MissingQueryParamRejection" in {
    Get() ~> wrap {
      parameters('amount, 'orderId) { (_, _) ⇒ completeOk }
    } ~> check {
      assert(status == NotFound)
      assert(responseAs[Error] == Error("NotFound", Some("Request is missing required query parameter 'amount'")))
    }
  }

  it should "respond with BadRequest for requests resulting in RequestEntityExpectedRejection" in {
    implicit val x = Unmarshaller.forNonEmpty[String]
    Post() ~> wrap {
      extractEntity(as[String]) { _ ⇒ completeOk }
    } ~> check {
      assert(status == BadRequest)
      assert(responseAs[Error] == Error("BadRequest", Some("Request entity expected but not supplied")))
    }
  }

  it should "respond with NotAcceptable for requests resulting in UnacceptedResponseContentTypeRejection" in {
    Get() ~> `Accept`(`text/css`) ~> `Accept-Encoding`(HttpEncodings.identity) ~> {
      wrap { complete("text text text") ~ encodeResponse(Gzip)(complete("test")) }
    } ~> check {
      assert(status == NotAcceptable)
      assert(responseAs[Error] == Error("NotAcceptable", Some("Resource representation is only available " +
        "with these Content-Types:\napplication/vnd.blinkbox.books.v2+json")))
    }
  }

  it should "respond with NotAcceptable for requests resulting in UnacceptedResponseEncodingRejection" in {
    Get() ~> `Accept-Encoding`(HttpEncodings.identity) ~> wrap {
      (encodeResponse(Gzip) | encodeResponse(Deflate)) { completeOk }
    } ~> check {
      assert(status == NotAcceptable)
      assert(responseAs[Error] == Error("NotAcceptable", Some("Resource representation is only available with these Content-Encodings:\ngzip\ndeflate")))
    }
  }

  it should "respond with UnsupportedMediaType for requests resulting in UnsupportedRequestContentTypeRejection" in {
    implicit val x = spray.httpx.marshalling.BasicMarshallers.HttpEntityMarshaller
    Post("/", HttpEntity(`application/pdf`, "...PDF...")) ~> wrap {
      extractEntity(as[NodeSeq]) { _ ⇒ completeOk }
    } ~> check {
      assert(status == UnsupportedMediaType)
      assert(responseAs[Error] == Error("UnsupportedMediaType", Some("There was a problem with the requests Content-Type:\n" +
        "Expected 'application/vnd.blinkbox.books.v2+json'")))
    }
  }

  it should "respond with BadRequest for requests resulting in UnsupportedRequestContentTypeRejection" in {
    Post("/", "Hello") ~> wrap {
      (decodeRequest(Gzip) | decodeRequest(Deflate)) { completeOk }
    } ~> check {
      assert(status == BadRequest)
      assert(responseAs[Error] == Error("BadRequest", Some("The requests Content-Encoding must be one the following:\ngzip\ndeflate")))
    }
  }

  it should "respond with BadRequest for requests resulting in a ValidationRejection" in {
    Get() ~> wrap {
      validate(false, "Oh noo!") { completeOk }
    } ~> check {
      assert(status == BadRequest)
      assert(responseAs[Error] == Error("BadRequest", Some("Oh noo!")))
    }
  }
}
