package io.scrapeyard

import io.scrapeyard.Models.{SearchParams, SearchYield}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.scalatest.selenium.Firefox

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

// val ff = new FirefoxDriver with Firefox
class QatarScraper extends Scraper with Firefox {

  val host = "http://www.qatarairways.com/global/en"

  override def scrape(ps: SearchParams): Try[SearchYield] = Try {
    val StringSearchParams(org, dst, dep, ret) = toStringSearchParams(ps)
    go to host

    implicitlyWait(20 seconds)

    eventually(assert(find("bookcont").isDefined))

    click on "book"
    enterDepartureAirport(org)

    enterDestinationAirport(dst)

    click on "departing"
    enter(dep)

    click on "returning"
    enter(ret)

    click on "bookFlight"

    val inboundSearch = eventually(timeout(2 minutes), interval(200 millis)) {
      find(cssSelector("#outbound_tripDetails1 .radiobtn")).get
    }

    click on inboundSearch
    click on cssSelector("#inbound_tripDetails1 .radiobtn")
    click on "indexItinerarayContinue"

//    if (find("warnFullSearch").isDefined || find("warnAvSearch").exists(_.isDisplayed))
//      throw new NonExistentConnectionException(ps.toString)


    val value = eventually(timeout(2 minutes), interval(200 millis)) {
      find("totalPrice").get.text
    }
    val currency = find(cssSelector("#totalPrice + .units")).get.text

    SearchYield(value.toDouble, currency, host)
  }

  def enterDepartureAirport(org: String): Unit = {
    textField("FromTemp").clear()
    click on "FromTemp"

    enterAndSelectFromDropDown(org)

    assert(find("remFrom").isDefined)
  }

  def enterDestinationAirport(dst: String): Unit = {
    textField("ToTemp").clear()
    click on "ToTemp"

    enterAndSelectFromDropDown(dst)

    assert(find("remTo").isDefined)
  }

  def enterAndSelectFromDropDown(airport: String): Unit = {
    enter(airport)

    val suggested = eventually {
      val suggested1 = find("ui-active-menuitem")
      assert(suggested1.get.isDisplayed)
      suggested1
    }

    if (suggested.get.text contains
      "There are no cities matching your request.")
      throw new NonExistentConnectionException(s"Airport not found: $airport.")

    assert(suggested.get.text contains s"($airport)")
    click on "ui-active-menuitem"
  }

  override protected def dateFormatter: DateTimeFormatter =
  DateTimeFormat.forPattern("dd-MMM-yyyy")
}