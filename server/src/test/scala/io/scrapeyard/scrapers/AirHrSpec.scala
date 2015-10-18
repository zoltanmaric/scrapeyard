package io.scrapeyard.scrapers

import io.scrapeyard.AirHrScraper
import org.scalatest.{Matchers, WordSpecLike}

class AirHrSpec extends WordSpecLike with Matchers with TestParams {
  "AirHr scraper" should {
    "find cheapest flight" in {
      val scraper = new AirHrScraper()
      val res = scraper.scrape(params).get
      res.currency should be("HRK")
      res.url should startWith("http://avio.air.hr")
      scraper.webDriver.quit()
    }
  }
}
