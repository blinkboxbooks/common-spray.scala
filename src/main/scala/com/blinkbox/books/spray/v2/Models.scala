package com.blinkbox.books.spray.v2

case class ListPage[T] (
  items: List[T],
  lastPage: Boolean
)