package io.scrapeyard

import org.scalatest.selenium.Firefox
import org.scalatest.time.{Seconds, Span}

// val ff = new FirefoxDriver with Firefox
object Scraper extends Firefox {

  val host = "http://www.qatarairways.com"

  def doIt(arrival: String): String = {
    go to host
    println(pageTitle)

    implicitlyWait(Span(20, Seconds))

    Thread.sleep(5000)

    click on "book"
    Thread.sleep(500)

    click on "FromTemp"
    Thread.sleep(500)
    enter("zagreb")
    Thread.sleep(500)
    enter("\t")

    Thread.sleep(500)
    enter("singapore")
    Thread.sleep(500)
    enter("\t")

    click on "departing"
    Thread.sleep(500)
    enter("21-May-2015")
    Thread.sleep(500)

    click on "returning"
    Thread.sleep(500)
    enter(arrival)
    Thread.sleep(500)

    click on "bookFlight"

    Thread.sleep(10000)

    find("tripGrandTotal").get.text
  }
}