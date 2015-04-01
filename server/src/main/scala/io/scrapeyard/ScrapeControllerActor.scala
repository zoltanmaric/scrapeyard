package io.scrapeyard

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.scrapeyard.Models._

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
    } yield SearchParams(orig, dest, dep, ret)

    searches.toVector
  }
}
