package io.scrapeyard

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.scrapeyard.Models.{SearchRequest, SearchResult}
import io.scrapeyard.ScrapeController.ControllerReq

import scala.language.postfixOps

class Dispatcher(scrapers: Map[String, ActorRef], mailerProps: Props) extends Actor
with ActorLogging {
  var reqEmail: String = _

  def receive: Receive = expectReq

  def expectReq: Receive = {
    case SearchRequest(email, criteria, scraperNamesOpt) =>
      val scraperNames = scraperNamesOpt.getOrElse(scrapers.keys.toSet)
      reqEmail = email
      val selectedScrapers = scrapers.filterKeys(scraperNames.contains).values.toSet
      val controllers = selectedScrapers.map(_ => context.actorOf(Props[ScrapeControllerActor]))
      controllers zip selectedScrapers foreach {
        case (c, s) => c ! ControllerReq(criteria, s)
      }
      context.become(expectResp(controllers, Set()))
  }

  private def updateState(expectedSenders: Set[ActorRef], receivedResults: Set[SearchResult]): Unit = {
    if (expectedSenders.isEmpty) {
      val mailer = context.actorOf(mailerProps)
      mailer ! SendResults(reqEmail, "Search results", receivedResults)
      context.stop(self)
    } else context.become(expectResp(expectedSenders, receivedResults))
  }

  private def expectResp(expectedSenders: Set[ActorRef], receivedResults: Set[SearchResult]): Receive = {
    case results: Set[SearchResult] =>
      updateState(expectedSenders - sender(), receivedResults ++ results)
  }
}
