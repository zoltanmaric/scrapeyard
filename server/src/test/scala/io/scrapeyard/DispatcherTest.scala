package io.scrapeyard

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import io.scrapeyard.Models._
import io.scrapeyard.ScrapeMailer.SendEmail
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpecLike}

import scala.util.Success

class DispatcherTest extends TestKit(ActorSystem("TestSys"))
with ImplicitSender with WordSpecLike with Matchers {

  "A dispatcher" when {
    "request message received" should {
      "dispatch request to all scrapers and send mail with response" in {
        val scraperProps = Set(Props[FakeScraperActor])
        val mailer = TestProbe()
        val mailerProps = Props(new Forwarder(mailer.ref))
        val dispatcher = system.actorOf(
          Props(new Dispatcher(scraperProps, mailerProps)), "dispatcher")

        val dep = DateTime.parse("2015-05-20T00:00:00Z")
        val ret = DateTime.parse("2015-07-20T00:00:00Z")
        // create batch search criteria with fixed departure airport,
        // departure and return dates, and two possible destination
        // airports
        val criteria = BatchSearchCriteria(
          Set("ZAG"),
          Set("BRU", "OST"),
          dep,
          dep,
          ret,
          ret
        )

        val req = SearchRequest("user@mail.com", criteria)

        dispatcher ! req

        val params1 = SearchParams("ZAG", "BRU", dep, ret)
        val params2 = SearchParams("ZAG", "OST", dep, ret)

        val results = Set(
          SearchResult(params1, SearchYield("100", "EUR")),
          SearchResult(params2, SearchYield("100", "EUR"))
        )

        import spray.json._
        import ModelsJsonSupport._

        val expected = SendEmail(
          "user@mail.com",
          "Search results",
          results.toJson.prettyPrint
        )

        mailer.expectMsg(expected)
      }
    }
  }
}

class Forwarder(target: ActorRef) extends Actor {
  def receive = { case m => target forward m }
}

class FakeScraperActor extends Actor {
  def receive = {
    case ps: SearchParams =>
      sender ! (ps, Success(SearchYield("100", "EUR")))
  }
}
