package io.scrapeyard

import io.scrapeyard.Models.SearchParams
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpecLike}

class IndividualScraperSpec extends WordSpecLike with Matchers {
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
    val scraper = QatarScraper
    val res = scraper.scrape(params).get
    res.price should endWith ("HRK")
    res.url should be ("http://www.qatarairways.com")
    scraper.close()(scraper.webDriver)
  }
}
