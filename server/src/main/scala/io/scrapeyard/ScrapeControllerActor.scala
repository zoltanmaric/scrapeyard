package io.scrapeyard

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.scrapeyard.Models.{BatchSearchCriteria, SearchResult}

import scala.util.Try

class ScrapeControllerActor extends Actor with ActorLogging {
  var numSearches = 0
  var results = Vector[Try[SearchResult]]()

  override def receive: Receive = awaitSearchRequest

  def awaitSearchRequest: Receive = {
    case SearchMsg(criteria, scraperRef) =>
      val paramList = Dispatcher.toSearchParams(criteria)
      // TODO: refactor SearchResult to a map of [SearchParams, (price, url)]
      numSearches = paramList.length
      paramList.foreach(scraperRef ! _)
      context.become(awaitResponses)

    case e => log.error("Unexpected message received: " + e)
  }

  def awaitResponses: Receive = {
    case res: Try[SearchResult] =>
      results :+= res
      numSearches -= 1
      if (numSearches == 0) {
        context.parent ! results
        context.stop(self)
      }

    case e => log.error("Unexpected message received: " + e)
  }
}

case class SearchMsg(criteria: BatchSearchCriteria, scraperRef: ActorRef)
