package io.scrapeyard

import io.scrapeyard.Models.SearchParams
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.util.Failure

class IndividualScraperSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  lazy val qatarScraper = QatarScraper

  val params = SearchParams(
    "ZAG",
    "DPS",
    DateTime.parse("2015-07-10T00:00:00Z"),
    DateTime.parse("2015-07-25T00:00:00Z")
  )

  "single search on air.hr scraper" in {
    val res = AirHrScraper.scrape(params).get
    res.price should endWith ("HRK")
    res.url should startWith ("http://avio.air.hr")
  }

  "single search on momondo scraper" in {
    val res = MomondoScraper.scrape(params).get
    res.price should endWith ("EUR")
    res.url should startWith ("http://www.momondo.com/flightsearch")
  }

  "single search on qatar scraper" in {
    val res = qatarScraper.scrape(params).get
    res.price should endWith ("HRK")
    res.url should be ("http://www.qatarairways.com")
  }

  "unavailable single search on qatar scraper throws illegal argument exception" in {
    val badParams = SearchParams(
      "LJU",    // Ljubljana
      "GIG",    // Rio de Janeiro
      DateTime.parse("2015-07-10T00:00:00Z"),
      DateTime.parse("2015-07-25T00:00:00Z")
    )

    val res = qatarScraper.scrape(badParams)

    res match {
      case Failure(_: IllegalArgumentException) => // expected
      case other => fail("Unexpected search outcome: " + other)
    }
  }

  override protected def afterAll(): Unit =
    qatarScraper.close()(qatarScraper.webDriver)
}
