package com.blinkbox.books.spray

import com.typesafe.scalalogging.Logger
import com.typesafe.scalalogging.slf4j.StrictLogging

object Playground extends App with StrictLogging {

  def logTest(logger: Logger) { logger.info("Second message") }

  logger.info("First message")
  logTest(logger)
}
