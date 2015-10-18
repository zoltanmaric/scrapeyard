package io.scrapeyard.scrapers

import io.scrapeyard.Models.SearchParams
import org.joda.time.DateTime

trait TestParams {
  val params = SearchParams(
    "ZAG",
    "DPS",
    DateTime.parse("2015-11-10T00:00:00Z"),
    DateTime.parse("2015-11-25T00:00:00Z")
  )
}
