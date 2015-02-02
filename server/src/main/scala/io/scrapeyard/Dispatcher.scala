package io.scrapeyard

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import io.scrapeyard.ScrapeMailer.SendEmail
import io.scrapeyard.Models.{SearchResult, BatchSearchCriteria, SearchParams, SearchRequest}
import spray.json._
import ModelsJsonSupport._

import scala.language.postfixOps
import scala.util.{Success, Failure, Try}

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

  def receive: Receive = {
    case req: SearchRequest => dispatch(req)
    case ControllerResp(req, results) =>
      val mailer = context.actorOf(mailerProps, "mailer")
      mailer ! SendEmail(req.email, "Search results", results.toJson.prettyPrint)
  }

  def dispatch(req: SearchRequest): Unit = {
    scrapers foreach { scraper =>
      val controller = context.actorOf(Props[ScrapeControllerActor])
      controller ! ControllerReq(req, scraper)
    }
  }
}
