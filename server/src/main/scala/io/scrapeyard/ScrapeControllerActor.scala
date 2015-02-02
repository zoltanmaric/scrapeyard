package io.scrapeyard

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.scrapeyard.Models._

import scala.util.{Failure, Success, Try}

class ScrapeControllerActor extends Actor with ActorLogging {
  import ScrapeController._

  var request: SearchRequest = _
  var paramSet = Set[SearchParams]()
  var results = Set[SearchResult]()

  override def receive: Receive = awaitSearchRequest

  def awaitSearchRequest: Receive = {
    case ControllerReq(req, scraperRef) =>
      request = req
      paramSet = toSearchParams(req.criteria).toSet
      paramSet.foreach(scraperRef ! _)
      context.become(awaitResponses)

    case e => log.error("Unexpected message received: " + e)
  }

  def awaitResponses: Receive = {
    case (params: SearchParams, resp: Try[SearchYield]) =>
      paramSet -= params
      resp match {
        case Success(yld) =>
          results += SearchResult(params, yld)
        case Failure(t) =>
          log.error(t, "Search failed for {}", params)
      }
      if (paramSet.isEmpty) {
        context.parent ! ControllerResp(request, results)
        context.stop(self)
      }

    case e => log.error("Unexpected message received: " + e)
  }
}

case class ControllerReq(req: SearchRequest, scraperRef: ActorRef)
case class ControllerResp(req: SearchRequest, results: Set[SearchResult])



object ScrapeController {
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
