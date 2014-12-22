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

  def doIt(ps: SearchParams): SearchResult = {
    go to host
    println(pageTitle)

    implicitlyWait(Span(20, Seconds))

    eventually(assert(find("bookcont").isDefined))

    click on "book"
    click on "FromTemp"
    enter(ps.origin)

    eventually {
      assert(find("ui-active-menuitem").get.isDisplayed)
    }
    click on "ui-active-menuitem"

    click on "ToTemp"
    enter(ps.destination)

    eventually {
      assert(find("ui-active-menuitem").get.isDisplayed)
    }
    click on "ui-active-menuitem"

    click on "departing"
    enter(fmt.print(ps.departure))

    click on "returning"
    enter(fmt.print(ps.returning))

    click on "bookFlight"

    val price = eventually(timeout(2 minutes)) {
      val total = find("tripGrandTotal").get.text
      total.length should be > 4
      total.replaceAll("""\s""", " ")
    }

    SearchResult(ps, price, host)
  }
}