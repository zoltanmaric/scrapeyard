package io.scrapeyard.scrapers

import io.scrapeyard.AirHrScraper
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class AirHrSpec extends WordSpecLike with Matchers with TestParams with BeforeAndAfterAll {
  "AirHr scraper" should {
    "find cheapest flight" in {
      val res = AirHrScraper.scrape(params).get
      res.currency should be("HRK")
      res.url should startWith("http://avio.air.hr")
    }
  }

  override def afterAll(): Unit = {
    AirHrScraper.webDriver.quit()
  }
}
