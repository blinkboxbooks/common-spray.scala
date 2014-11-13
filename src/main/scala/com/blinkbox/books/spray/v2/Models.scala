package com.blinkbox.books.spray.v2

import java.net.URI

case class ListPage[T] (
  items: List[T],
  lastPage: Boolean
)

trait Relation

case class Link(rel: Relation, url: URI)