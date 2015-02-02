package io.scrapeyard

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.scrapeyard.Models._

import scala.util.{Failure, Success, Try}

class ScrapeControllerActor extends Actor with ActorLogging {
  var request: SearchRequest = _
  var paramSet = Set[SearchParams]()
  var results = Set[SearchResult]()

  override def receive: Receive = awaitSearchRequest

  def awaitSearchRequest: Receive = {
    case ControllerReq(req, scraperRef) =>
      request = req
      paramSet = Dispatcher.toSearchParams(req.criteria).toSet
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
