package io.scrapeyard.scrapers

import io.scrapeyard.MomondoScraper
import org.scalatest.{Matchers, WordSpecLike}

class MomondoSpec extends WordSpecLike with Matchers with TestParams {
  "Momondo scraper should" in {
    val res = MomondoScraper.scrape(params).get
    res.currency should be ("EUR")
    res.url should startWith ("http://www.momondo.com/flightsearch")
  }
}
