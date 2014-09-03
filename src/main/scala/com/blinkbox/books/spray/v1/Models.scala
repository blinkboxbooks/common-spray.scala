package com.blinkbox.books.spray.v1

case class ListPage[T](
  numberOfResults: Int,
  offset: Int,
  count: Int,
  items: List[T],
  links: Option[List[Link]] = None)

case class Link(
  rel: String,
  href: String,
  title: Option[String],
  targetGuid: Option[String] = None)

case class Image(rel: String, src: String)

case class Error(code: String, message: String)
