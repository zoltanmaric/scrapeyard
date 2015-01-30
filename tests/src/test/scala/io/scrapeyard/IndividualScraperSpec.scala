package io.scrapeyard

import io.scrapeyard.Models.SearchParams
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpecLike}

class IndividualScraperSpec extends WordSpecLike with Matchers {
  "single search on air.hr scraper" in {
    val params = SearchParams(
      "ZAG",
      "DPS",
      DateTime.parse("2015-07-10T00:00:00Z"),
      DateTime.parse("2015-07-25T00:00:00Z")
    )

    val res = AirHrScraper.scrape(params).get
    res.price should endWith ("HRK")
    res.url should startWith ("http://air.hr")
  }
}
