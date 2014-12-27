package io.scrapeyard

object Dispatcher {
  def toSearchParams(criteria: BatchSearchCriteria): Seq[SearchParams] = {
    var depDates = Vector(criteria.depFrom)
    while(depDates.last.compareTo(criteria.depUntil) < 0) {
      depDates = depDates :+ depDates.last.plusDays(1)
    }

    var retDates = Vector(criteria.retFrom)
    while(retDates.last.compareTo(criteria.retUntil) < 0) {
      retDates = retDates :+ retDates.last.plusDays(1)
    }


    val searches = for {
      orig <- criteria.origs
      dest <- criteria.dests
      dep <- depDates
      ret <- retDates
    } yield SearchParams(orig, dest, dep.toInstant, ret.toInstant)

    searches.toVector
  }
}
