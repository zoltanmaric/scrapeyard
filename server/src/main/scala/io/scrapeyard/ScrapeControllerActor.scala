package io.scrapeyard

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.scrapeyard.Models._
import org.joda.time.{Interval, Period}

import scala.util.{Failure, Success, Try}

class ScrapeControllerActor extends Actor with ActorLogging {
  import ScrapeController._

  var paramSet = Set[SearchParams]()
  var results = Set[SearchResult]()

  override def receive: Receive = awaitSearchRequest

  def awaitSearchRequest: Receive = {
    case ControllerReq(criteria, scraperRef) =>
      paramSet = toSearchParams(criteria).toSet
      paramSet.foreach(scraperRef ! _)
      context.become(awaitResponses)

    case e => log.error("Unexpected message received: " + e)
  }

  def awaitResponses: Receive = {
    case (params: SearchParams, resp: Try[SearchYield]) =>
      paramSet -= params
      resp match {
        case Success(yld) =>
          val rslt = SearchResult(params, yld)
          results += rslt
          log.info(rslt.toString)
        case Failure(ne: NonExistentConnectionException) =>
          log.info("Search failed for {}: {}", params, ne.getMessage)
        case Failure(t) =>
          log.error(t, "Search failed for {}", params)
      }
      if (paramSet.isEmpty) {
        context.parent ! results
        context.stop(self)
      }

    case e => log.error("Unexpected message received: " + e)
  }
}

object ScrapeController {
  case class ControllerReq(criteria: BatchSearchCriteria, scraperRef: ActorRef)

  def toSearchParams(criteria: BatchSearchCriteria): Seq[SearchParams] = {
    val depDates = Stream.iterate(criteria.depFrom)(_.plusDays(1))
      .takeWhile(_.compareTo(criteria.depUntil) <= 0)
    val periods = depDates.flatMap{ dep =>
      for {
        days <- criteria.minStay to criteria.maxStay
        ret = dep.plusDays(days)
        retUntil = criteria.retUntil
        retFrom = criteria.retFrom
        if !retFrom.isAfter(ret) && !retUntil.isBefore(ret)
      } yield (dep, ret)
    }

    val searches = for {
      orig <- criteria.origs
      dest <- criteria.dests
      (dep, ret) <- periods
    } yield SearchParams(orig, dest, dep, ret)

    searches.toVector
  }
}
