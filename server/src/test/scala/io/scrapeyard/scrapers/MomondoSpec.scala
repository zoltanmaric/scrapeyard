package io.scrapeyard.scrapers

import io.scrapeyard.MomondoScraper
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class MomondoSpec extends WordSpecLike with Matchers with TestParams with BeforeAndAfterAll {
  "Momondo scraper" should {
    "find cheapest flight" in {
      val res = MomondoScraper.scrape(params).get
      res.currency should be("EUR")
      res.url should startWith("http://www.momondo.com/flightsearch")
    }
  }

  override def afterAll(): Unit = {
    MomondoScraper.webDriver.quit()
  }
}
