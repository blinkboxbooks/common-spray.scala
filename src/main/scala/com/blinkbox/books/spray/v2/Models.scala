package com.blinkbox.books.spray.v2

import java.net.URI

case class ListPage[T] (
  items: List[T],
  lastPage: Boolean
)

case class Link(`type`: String, url: URI)