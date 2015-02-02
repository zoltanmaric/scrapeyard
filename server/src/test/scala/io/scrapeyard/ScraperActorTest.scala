package io.scrapeyard

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import io.scrapeyard.Models.{SearchParams, SearchYield}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.scalatest.{BeforeAndAfter, WordSpecLike}

import scala.util.{Failure, Success, Try}

class ScraperActorTest extends TestKit(ActorSystem("TestSys"))
with ImplicitSender with WordSpecLike with BeforeAndAfter {
  val params = SearchParams(
    "ZAG",
    "BRU",
    DateTime.parse("2015-05-20T00:00:00Z"),
    DateTime.parse("2015-07-20T00:00:00Z")
  )

  trait FakeScraper extends Scraper {
    // not required
    override protected def dateFormatter: DateTimeFormatter = _
  }

  "A scraper actor" when {

    "scrape succeeds" should {
      "return a success message" in {
        val success = Success(SearchYield("2 USD", "url1"))

        val successScraper = new FakeScraper {
          override def scrape(ps: SearchParams): Try[SearchYield] = success
        }

        val scraper = system.actorOf(Props(new ScraperActor(successScraper)), "successScraper")
        scraper ! params
        expectMsg((params, success))
      }
    }

    "scrape fails" should {
      "return a failure message" in {
        val failure = Failure(new Exception())

        val failScraper = new FakeScraper {
          override def scrape(ps: SearchParams): Try[SearchYield] = failure
        }

        val scraper = system.actorOf(Props(new ScraperActor(failScraper)), "failScraper")
        scraper ! params
        expectMsg((params, failure))
      }
    }
  }
}
