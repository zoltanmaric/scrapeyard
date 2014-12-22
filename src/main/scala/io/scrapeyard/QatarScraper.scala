package io.scrapeyard

import org.joda.time.format.DateTimeFormat
import org.openqa.selenium.WebDriver
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

    eventually(timeout(2 minutes)) {
      click on "book"
      eventually(timeout(5 seconds))(assert(find("FromTemp").get.isDisplayed))
    }

    click on "FromTemp"
    eventually {
      enter(ps.origin)
      assert(find("ui-active-menuitem").get.isDisplayed)
    }
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

    val price = eventually(timeout(2 minutes)) {
      val total = find("tripGrandTotal").get.text
      total.length should be > 4
      total
    }

    SearchResult(ps, price, host)
  }


  /** * Ensure the page is loaded and AJAX calls are completed using jQuery. */
  def pageIsLoadedAndAjaxIsCompleted()(implicit driver: WebDriver) {
    eventually {
      withClue("Ajax calls may not have completed within time specified") {
        executeScript("return jQuery.active")
          .asInstanceOf[Long] shouldBe (0)
      }
    }
    eventually {
      withClue("Document ready state was not [complete] within time specified by eventually clause.") {
        executeScript("return document.readyState")
          .asInstanceOf[String] shouldEqual ("complete")
      }
    }
  }
}