package com.blinkbox.books.spray.v1

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}
import scala.annotation.meta.field

@ApiModel(description = "A page in a list of results")
case class ListPage[T](
  @(ApiModelProperty @field)(position = 0, value = "The total number of results available in the list") numberOfResults: Int,
  @(ApiModelProperty @field)(position = 1, value = "The offset this page is into the list") offset: Int,
  @(ApiModelProperty @field)(position = 2, value = "The number of results in this page") count: Int,
  @(ApiModelProperty @field)(position = 3, value = "The items in the page") items: List[T],
  @(ApiModelProperty @field)(position = 4, value = "Links relating to the list") links: Option[List[Link]] = None)

@ApiModel(description = "A link to another resource")
case class Link(
  @(ApiModelProperty @field)(position = 0, value = "The relationship of the other resource to this one") rel: String,
  @(ApiModelProperty @field)(position = 1, value = "The link to the resource") href: String,
  @(ApiModelProperty @field)(position = 2, value = "The title of the linked resource") title: Option[String],
  @(ApiModelProperty @field)(position = 3, value = "The guid of the linked resource") targetGuid: Option[String] = None)

@ApiModel(description = "A link to an image")
case class Image(
  @(ApiModelProperty @field)(position = 0, value = "The relationship of the image to this resource") rel: String,
  @(ApiModelProperty @field)(position = 1, value = "The source URL for the image") src: String)

@ApiModel(description = "Information about an error")
case class Error(
  @(ApiModelProperty @field)(position = 0, value = "The error code") code: String,
  @(ApiModelProperty @field)(position = 1, value = "A description of the error") message: String)
