package io.scrapeyard

import akka.actor._
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import io.scrapeyard.Models._
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.util.Success

class DispatcherTest extends TestKit(ActorSystem("TestSys"))
with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  "A dispatcher" when {
    "request message received" should {
      "dispatches request to all scrapers and sends mail with results" in {
        val scraperProps = Set(
          Props(new FakeScraperActor("100", "EUR")),
          Props(new FakeScraperActor("200", "USD"))
        )
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
          SearchResult(params2, SearchYield("100", "EUR")),
          SearchResult(params1, SearchYield("200", "USD")),
          SearchResult(params2, SearchYield("200", "USD"))
        )

        val expected = SendResults(
          "user@mail.com",
          "Search results",
          results
        )

        mailer.expectMsg(expected)

        import scala.concurrent.duration._
        import scala.language.postfixOps
        val dispWatch = TestProbe()
        dispWatch watch dispatcher
        // verify that actor is stopped
        dispWatch.expectMsgPF(2 seconds) { case Terminated(_) => true }
      }
    }
  }

  override protected def afterAll(): Unit = system.shutdown()
}

class Forwarder(target: ActorRef) extends Actor {
  def receive = { case m => target forward m }
}

class FakeScraperActor(amount: String, currency: String) extends Actor {
  def receive = {
    case ps: SearchParams =>
      sender ! (ps, Success(SearchYield(amount, currency)))
  }
}

