package io.scrapeyard

import org.joda.time.format.DateTimeFormat
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually
import org.scalatest.selenium.Firefox
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.duration._

// val ff = new FirefoxDriver with Firefox
object QatarScraper extends Firefox with Matchers with Eventually {

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

//    Thread.sleep(10000)

    eventually(timeout(2 minutes)) {
      val total = find("tripGrandTotal").get.text
      total.length should be > 4
      total
    }
  }
}