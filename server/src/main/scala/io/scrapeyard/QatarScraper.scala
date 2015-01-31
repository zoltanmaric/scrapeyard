package io.scrapeyard

import io.scrapeyard.Models.{SearchParams, SearchResult}
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually
import org.scalatest.selenium.Firefox

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

// val ff = new FirefoxDriver with Firefox
object QatarScraper extends Scraper with Firefox {

  val host = "http://www.qatarairways.com"

  def scrape(ps: SearchParams): Try[SearchResult] = Try {
    val StringSearchParams(org, dst, dep, ret) = toStringSearchParams(ps)
    go to host

    implicitlyWait(20 seconds)

    eventually(assert(find("bookcont").isDefined))

    click on "book"
    click on "FromTemp"
    enter(org)

    eventually {
      assert(find("ui-active-menuitem").get.isDisplayed)
    }
    click on "ui-active-menuitem"

    click on "ToTemp"
    enter(dst)

    eventually {
      assert(find("ui-active-menuitem").get.isDisplayed)
    }
    click on "ui-active-menuitem"

    click on "departing"
    enter(dep)

    click on "returning"
    enter(ret)

    click on "bookFlight"

    val price = eventually(timeout(2 minutes), interval(200 millis)) {
      val total = find("tripGrandTotal").get.text
      total.length should be > 4
      total.replaceAll("""\s""", " ")
    }

    SearchResult(ps, price, host)
  }

  override protected def dateFormatter: DateTimeFormatter =
    DateTimeFormat.forPattern("dd-MMM-yyyy")
}