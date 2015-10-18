package io.scrapeyard.scrapers

import io.scrapeyard.Models.SearchParams
import io.scrapeyard.{NonExistentConnectionException, QatarScraper}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.util.Failure

class QatarSpec extends WordSpecLike with Matchers with TestParams with BeforeAndAfterAll {
  "Qatar scraper" should {
    "find cheapest flight" in {
      val res = QatarScraper.scrape(params).get
      res.currency should be ("HRK")
      res.url shouldEqual QatarScraper.host
    }

    "throw illegal argument exception when single search unavailable in drop-down" in {
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

    "throws non-existent connection exception when single search unavailable after submit" in {
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
  }

  override def afterAll(): Unit = {
    QatarScraper.webDriver.quit()
  }
}
