package com.blinkbox.books.spray.v2

import spray.http.ContentRange
import spray.http.HttpHeaders._
import spray.http.StatusCodes._
import spray.routing.AuthenticationFailedRejection.{CredentialsRejected, CredentialsMissing}
import spray.routing._
import spray.routing.{RejectionHandler => SprayRejectionHandler}
import spray.routing.directives.RouteDirectives._

object RejectionHandler extends JsonSupport {

  val ErrorRejectionHandler: SprayRejectionHandler = SprayRejectionHandler {
    case Nil => complete(NotFound, Error(NotFound))

    case AuthenticationFailedRejection(cause, challengeHeaders) :: _ =>
      val rejectionMessage = cause match {
        case CredentialsMissing  => "The resource requires authentication, which was not supplied with the request"
        case CredentialsRejected => "The supplied authentication is invalid"
      }
      ctx => ctx.complete(Unauthorized, challengeHeaders, Error(Unauthorized.reason.replace(" ", ""), Some(rejectionMessage)))

    case AuthorizationFailedRejection :: _ =>
      complete(Forbidden, Error("Forbidden", Some("The supplied authentication is not authorized to access this resource")))

    case CorruptRequestEncodingRejection(msg) :: _ =>
      complete(BadRequest, Error("BadRequest", Some("The requests encoding is corrupt:\n" + msg)))

    case MalformedFormFieldRejection(name, msg, _) :: _ =>
      complete(BadRequest, Error("BadRequest", Some("The form field '" + name + "' was malformed:\n" + msg)))

    case MalformedHeaderRejection(headerName, msg, _) :: _ =>
      complete(BadRequest, Error("BadRequest", Some(s"The value of HTTP header '$headerName' was malformed:\n" + msg)))

    case MalformedQueryParamRejection(name, msg, _) :: _ =>
      complete(BadRequest, Error("BadRequest", Some("The query parameter '" + name + "' was malformed:\n" + msg)))

    case MalformedRequestContentRejection(msg, _) :: _ =>
      complete(BadRequest, Error("BadRequest", Some("The request content was malformed:\n" + msg)))

    case rejections @ (MethodRejection(_) :: _) =>
      val methods = rejections.collect { case MethodRejection(method) => method }
      complete(MethodNotAllowed, List(Allow(methods: _*)), Error("MethodNotAllowed", Some("HTTP method not allowed, supported methods: " + methods.mkString(", "))))

    case rejections @ (SchemeRejection(_) :: _) =>
      val schemes = rejections.collect { case SchemeRejection(scheme) => scheme }
      complete(BadRequest, Error("BadRequest", Some("Uri scheme not allowed, supported schemes: " + schemes.mkString(", "))))

    case MissingCookieRejection(cookieName) :: _ =>
      complete(BadRequest, Error("BadRequest", Some("Request is missing required cookie '" + cookieName + '\'')))

    case MissingFormFieldRejection(fieldName) :: _ =>
      complete(BadRequest, Error("BadRequest", Some("Request is missing required form field '" + fieldName + '\'')))

    case MissingHeaderRejection(headerName) :: _ =>
      complete(BadRequest, "Request is missing required HTTP header '" + headerName + '\'')

    case MissingQueryParamRejection(paramName) :: _ =>
      complete(NotFound, Error("NotFound", Some("Request is missing required query parameter '" + paramName + '\'')))

    case RequestEntityExpectedRejection :: _ =>
      complete(BadRequest, Error("BadRequest", Some("Request entity expected but not supplied")))

    case TooManyRangesRejection(_) :: _ =>
      complete(RequestedRangeNotSatisfiable, "Request contains too many ranges.")

    case UnsatisfiableRangeRejection(unsatisfiableRanges, actualEntityLength) :: _ =>
      complete(RequestedRangeNotSatisfiable, List(`Content-Range`(ContentRange.Unsatisfiable(actualEntityLength))),
        unsatisfiableRanges.mkString("None of the following requested Ranges were satisfiable:\n", "\n", ""))

    case rejections @ (UnacceptedResponseContentTypeRejection(_) :: _) =>
      val supported = rejections.flatMap {
        case UnacceptedResponseContentTypeRejection(supported) => supported
        case _ => Nil
      }
      complete(NotAcceptable, Error("NotAcceptable", Some("Resource representation is only available with these Content-Types:\n" + supported.map(_.value).mkString("\n"))))

    case rejections @ (UnacceptedResponseEncodingRejection(_) :: _) =>
      val supported = rejections.collect { case UnacceptedResponseEncodingRejection(supported) => supported }
      complete(NotAcceptable, Error("NotAcceptable", Some("Resource representation is only available with these Content-Encodings:\n" + supported.map(_.value).mkString("\n"))))

    case rejections @ (UnsupportedRequestContentTypeRejection(_) :: _) =>
      val supported = rejections.collect { case UnsupportedRequestContentTypeRejection(supported) => supported }
      complete(UnsupportedMediaType, Error("UnsupportedMediaType", Some("There was a problem with the requests Content-Type:\n" + supported.mkString(" or "))))

    case rejections @ (UnsupportedRequestEncodingRejection(_) :: _) =>
      val supported = rejections.collect { case UnsupportedRequestEncodingRejection(supported) => supported }
      complete(BadRequest, Error("BadRequest", Some("The requests Content-Encoding must be one the following:\n" + supported.map(_.value).mkString("\n"))))

    case ValidationRejection(msg, _) :: _ =>
      complete(BadRequest, Error("BadRequest", Some(msg)))
  }
}