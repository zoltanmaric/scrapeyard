package io.scrapeyard

import io.scrapeyard.Models.BatchSearchCriteria
import org.joda.time.{Duration, DateTime}
import org.scalatest.{Matchers, WordSpecLike}

class ScrapeControllerTest extends WordSpecLike with Matchers {
  "creates a search for each day in the time span" in {
    val criteria = BatchSearchCriteria(
      Set("ZAG"),
      Set("DPS"),
      DateTime.parse("2015-05-20T00:00:00Z"),
      DateTime.parse("2015-05-21T00:00:00Z"),
      DateTime.parse("2015-07-20T00:00:00Z"),
      DateTime.parse("2015-07-31T00:00:00Z"),
      minStayDays = 0,
      maxStayDays = 100
    )

    val searches = ScrapeController.toSearchParams(criteria)
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


  "start date less or equal to end date in the time span" in {
    val criteria = BatchSearchCriteria(
      Set("ZAG"),
      Set("DPS"),
      DateTime.parse("2015-11-20T00:00:00Z"),
      DateTime.parse("2015-11-21T00:00:00Z"),
      DateTime.parse("2015-11-20T00:00:00Z"),
      DateTime.parse("2015-11-20T00:00:00Z"),
      minStayDays = 0,
      maxStayDays = 1
    )

    val searches = ScrapeController.toSearchParams(criteria)
    // only one possible solution - departing and returning on the 20th
    searches.size should be (1)

    searches foreach { s =>
      assert(s.origin === "ZAG")
      assert(s.destination === "DPS")
      assert(s.departure.compareTo(criteria.depFrom) >= 0)
      assert(s.departure.compareTo(criteria.depUntil) <= 0)
      assert(s.returning.compareTo(criteria.retFrom) >= 0)
      assert(s.returning.compareTo(criteria.retUntil) <= 0)
    }
  }

  "creates 7 week-long stay searches" in {
    val criteria = BatchSearchCriteria(
      Set("ZAG"),
      Set("DPS"),
      DateTime.parse("2015-05-21T00:00:00Z"),
      DateTime.parse("2015-05-27T00:00:00Z"),
      DateTime.parse("2015-05-28T00:00:00Z"),
      DateTime.parse("2015-06-03T00:00:00Z"),
      minStayDays = 7,
      maxStayDays = 7
    )

    val searches = ScrapeController.toSearchParams(criteria)
    // the stay must be 7 days, and there are 7 possible departure and return dates
    searches.size should be (7)

    searches foreach { s =>
      assert(s.origin === "ZAG")
      assert(s.destination === "DPS")
      assert(new Duration(s.departure, s.returning).toStandardDays.getDays == 7)
    }
  }
}
