package io.scrapeyard

import io.scrapeyard.Models.BatchSearchCriteria
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpecLike}

class DispatcherTest extends WordSpecLike with Matchers {
  // TODO: add some property-based tests

  "creates a search for each day in the time span" in {
    val criteria = BatchSearchCriteria(
      Set("ZAG"),
      Set("DPS"),
      DateTime.parse("2015-05-20T00:00:00Z"),
      DateTime.parse("2015-05-21T00:00:00Z"),
      DateTime.parse("2015-07-20T00:00:00Z"),
      DateTime.parse("2015-07-31T00:00:00Z")
    )

    val searches = Dispatcher.toSearchParams(criteria)
    // two departure dates, 12 return dates
    searches.size should be (2 * 12)

    searches foreach { s =>
      assert(s.origin === "ZAG")
      assert(s.destination === "DPS")
      assert(s.departure.compareTo(criteria.depFrom) >= 0)
      assert(s.departure.compareTo(criteria.depUntil) <= 0)
      assert(s.returning.compareTo(criteria.retFrom) >= 0)
      assert(s.returning.compareTo(criteria.retUntil) <= 0)
    }
  }
}
