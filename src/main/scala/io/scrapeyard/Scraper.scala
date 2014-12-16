package io.scrapeyard

import org.scalatest.selenium.Firefox

object Scraper extends Firefox {

  val host = "http://lovenirs.com"

  def doIt: Unit = {
    go to host
    println(pageTitle)
  }
}