package io.scrapeyard

import org.joda.time.format.DateTimeFormat
import org.scalatest.selenium.Firefox
import org.scalatest.time.{Seconds, Span}

// val ff = new FirefoxDriver with Firefox
object QatarScraper extends Firefox {

  val host = "http://www.qatarairways.com"
  val fmt = DateTimeFormat.forPattern("dd-MMM-yyyy")

  def doIt(ps: SearchParams): String = {
    go to host
    println(pageTitle)

    implicitlyWait(Span(20, Seconds))

    Thread.sleep(5000)

    click on "book"
    Thread.sleep(500)

    click on "FromTemp"
    Thread.sleep(500)
    enter(ps.origin)
    Thread.sleep(500)
    enter("\t")

    Thread.sleep(500)
    enter(ps.destination)
    Thread.sleep(500)
    enter("\t")

    click on "departing"
    Thread.sleep(500)
    enter(fmt.print(ps.departure))
    Thread.sleep(500)

    click on "returning"
    Thread.sleep(500)
    enter(fmt.print(ps.returning))
    Thread.sleep(500)

    click on "bookFlight"

    Thread.sleep(10000)

    find("tripGrandTotal").get.text
  }
}