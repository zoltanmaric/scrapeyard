package io.scrapeyard

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.scrapeyard.Models.{SearchRequest, SearchResult}
import io.scrapeyard.ScrapeController.ControllerReq

import scala.language.postfixOps

class Dispatcher(scrapers: Map[String, ActorRef], mailerProps: Props) extends Actor
with ActorLogging {

  var controllers = Set[ActorRef]()
  var responseMap = Map[ActorRef, Set[SearchResult]]()
  var reqEmail: String = _

  def receive: Receive = expectReq

  def expectReq: Receive = {
    case SearchRequest(email, criteria, scraperNamesOpt) =>
      val scraperNames = scraperNamesOpt.getOrElse(scrapers.keys.toSet)
      reqEmail = email
      val selectedScrapers = scrapers.filterKeys(scraperNames.contains).values.toSet
      controllers = selectedScrapers.map(_ => context.actorOf(Props[ScrapeControllerActor]))
      controllers zip selectedScrapers foreach {
        case (c, s) => c ! ControllerReq(criteria, s)
      }
      context.become(expectResp)
  }

  def expectResp: Receive = {
    case results: Set[SearchResult] =>
      responseMap += (sender -> results)
      if (responseMap.keys == controllers) {
        val results = responseMap.flatMap {
          case (_, results1) => results1
        }.toSet
        val mailer = context.actorOf(mailerProps)
        mailer ! SendResults(reqEmail, "Search results", results)
        context.stop(self)
      }
  }
}
