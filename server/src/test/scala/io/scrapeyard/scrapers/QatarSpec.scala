package io.scrapeyard.scrapers

import io.scrapeyard.Models.SearchParams
import io.scrapeyard.{NonExistentConnectionException, QatarScraper}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.util.Failure

class QatarSpec extends WordSpecLike with Matchers with TestParams with BeforeAndAfterAll {
  lazy val scraper = new QatarScraper()
  "Qatar scraper" should {
    "find cheapest flight" in {
      val res = scraper.scrape(params).get
      res.currency should be ("HRK")
      res.url shouldEqual scraper.host
    }

    "throw illegal argument exception when single search unavailable in drop-down" ignore {
      val badParams = SearchParams(
        "LJU",    // Ljubljana
        "GIG",    // Rio de Janeiro
        DateTime.parse("2016-07-10T00:00:00Z"),
        DateTime.parse("2016-07-25T00:00:00Z")
      )

      val res = scraper.scrape(badParams)

      res match {
        case Failure(_: NonExistentConnectionException) => // expected
        case other => fail("Unexpected search outcome: " + other)
      }
    }

    "throw non-existent connection exception when single search unavailable after submit" ignore {
      val badParams = SearchParams(
        "ZAG",    // Zagreb
        "GIG",    // Rio de Janeiro
        DateTime.parse("2016-07-04T00:00:00Z"),
        DateTime.parse("2016-07-05T00:00:00Z")
      )

      val res = scraper.scrape(badParams)

      res match {
        case Failure(_: NonExistentConnectionException) => // expected
        case other => fail("Unexpected search outcome: " + other)
      }
    }
  }

  override def afterAll(): Unit = {
    scraper.webDriver.quit()
  }
}
