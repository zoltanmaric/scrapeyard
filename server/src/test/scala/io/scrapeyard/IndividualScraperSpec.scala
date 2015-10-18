package io.scrapeyard

import io.scrapeyard.Models.SearchParams
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.util.Failure

class IndividualScraperSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  val params = SearchParams(
    "ZAG",
    "DPS",
    DateTime.parse("2015-11-10T00:00:00Z"),
    DateTime.parse("2015-11-25T00:00:00Z")
  )

  "single search on air.hr scraper" in {
    val res = AirHrScraper.scrape(params).get
    res.currency should be ("HRK")
    res.url should startWith ("http://avio.air.hr")
  }

  "single search on momondo scraper" in {
    val res = MomondoScraper.scrape(params).get
    res.currency should be ("EUR")
    res.url should startWith ("http://www.momondo.com/flightsearch")
  }

  "single search on qatar scraper" in {
    val res = QatarScraper.scrape(params).get
    res.currency should be ("HRK")
    res.url shouldEqual QatarScraper.host
  }

  "single search unavailable in drop-down on qatar scraper throws illegal argument exception" in {
    val badParams = SearchParams(
      "LJU",    // Ljubljana
      "GIG",    // Rio de Janeiro
      DateTime.parse("2016-07-10T00:00:00Z"),
      DateTime.parse("2016-07-25T00:00:00Z")
    )

    val res = QatarScraper.scrape(badParams)

    res match {
      case Failure(_: NonExistentConnectionException) => // expected
      case other => fail("Unexpected search outcome: " + other)
    }
  }

  "single search unavailable after submit on qatar scraper throws non-existent connection exception" in {
    val badParams = SearchParams(
      "ZAG",    // Zagreb
      "GIG",    // Rio de Janeiro
      DateTime.parse("2016-07-04T00:00:00Z"),
      DateTime.parse("2016-07-05T00:00:00Z")
    )

    val res = QatarScraper.scrape(badParams)

    res match {
      case Failure(_: NonExistentConnectionException) => // expected
      case other => fail("Unexpected search outcome: " + other)
    }
  }

  override def afterAll(): Unit = {
    QatarScraper.webDriver.quit()
    AirHrScraper.webDriver.quit()
    MomondoScraper.webDriver.quit()
  }
}
