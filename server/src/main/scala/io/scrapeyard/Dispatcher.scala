package io.scrapeyard

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.scrapeyard.Models.SearchRequest

import scala.language.postfixOps

class Dispatcher(scraperProps: Set[Props], mailerProps: Props) extends Actor
with ActorLogging {

  def this() {
    this(
      Set(
        Props(new ScraperActor(AirHrScraper)),
        Props(new ScraperActor(MomondoScraper)),
        Props(new ScraperActor(QatarScraper))
      ),
      Props[MailerActor]
    )
  }

  val scrapers = scraperProps map {
    prop => context.actorOf(prop)
  }

  var controllers = Set[ActorRef]()
  var responseMap = Map[ActorRef, ControllerResp]()
  var reqEmail: String = _

  def receive: Receive = expectReq

  def expectReq: Receive = {
    case req: SearchRequest =>
      reqEmail = req.email
      controllers = scrapers.map(_ => context.actorOf(Props[ScrapeControllerActor]))
      controllers zip scrapers foreach {
        case (c, s) => c ! ControllerReq(req, s)
      }
      context.become(expectResp)
  }

  def expectResp: Receive = {
    case resp: ControllerResp =>
      responseMap += (sender -> resp)
      if (responseMap.keys == controllers) {
        val results = responseMap.flatMap {
          case (_, ControllerResp(_, res)) => res
        }.toSet
        val mailer = context.actorOf(mailerProps)
        mailer ! SendResults(reqEmail, "Search results", results)
        context.stop(self)
      }
  }
}
