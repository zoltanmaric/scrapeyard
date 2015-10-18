package io.scrapeyard.scrapers

import io.scrapeyard.MomondoScraper
import org.scalatest.{Matchers, WordSpecLike}

class MomondoSpec extends WordSpecLike with Matchers with TestParams {
  "Momondo scraper" should {
    "find cheapest flight" in {
      val scraper = new MomondoScraper()
      val res = scraper.scrape(params).get
      res.currency should be("EUR")
      res.url should startWith("http://www.momondo.com/flightsearch")
      scraper.webDriver.quit()
    }
  }
}
