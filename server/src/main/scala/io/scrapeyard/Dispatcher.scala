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

object Dispatcher {
  // TODO: move to scrape controller
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
