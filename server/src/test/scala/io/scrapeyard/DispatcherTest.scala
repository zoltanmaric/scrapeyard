package io.scrapeyard

import akka.actor._
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import io.scrapeyard.Models._
import org.joda.time.{Duration, DateTime}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

import scala.util.Success

class DispatcherTest extends TestKit(ActorSystem("TestSys"))
with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  "A dispatcher" when {
    "request message received" should {
      "dispatch request to all scrapers and send mail with results" in {
        val scrapers = Map(
          "fake" -> system.actorOf(Props(new FakeScraperActor("100", "EUR"))),
          "fake2" -> system.actorOf(Props(new FakeScraperActor("200", "USD")))
        )
        val mailer = TestProbe()
        val mailerProps = Props(new Forwarder(mailer.ref))
        val dispatcher = system.actorOf(
          Props(new Dispatcher(scrapers, mailerProps)), "dispatcher")

        val dep = DateTime.parse("2015-05-20T00:00:00Z")
        val ret = DateTime.parse("2015-07-20T00:00:00Z")
        val stayDays = new Duration(dep, ret).toStandardDays.getDays
        // create batch search criteria with fixed departure airport,
        // departure and return dates, and two possible destination
        // airports
        val criteria = BatchSearchCriteria(
          Set("ZAG"),
          Set("BRU", "OST"),
          dep,
          dep,
          ret,
          ret,
          stayDays,
          stayDays
        )

        val req = SearchRequest("user@mail.com", criteria)

        dispatcher ! req

        val params1 = SearchParams("ZAG", "BRU", dep, ret)
        val params2 = SearchParams("ZAG", "OST", dep, ret)

        val results = Set(
          SearchResult(params1, SearchYield(100, "EUR", "url")),
          SearchResult(params2, SearchYield(100, "EUR", "url")),
          SearchResult(params1, SearchYield(200, "USD", "url")),
          SearchResult(params2, SearchYield(200, "USD", "url"))
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

      "dispatch request only to selected scrapers" in {
        val scraperActors = (1 to 3).map(_ => TestProbe())
        val Seq(momondo, airHr, qatar) = scraperActors
        val scrapers = Map(
          "momondo" -> momondo.ref,
          "airHr" -> airHr.ref,
          "qatar" -> qatar.ref
        )
        val dispatcher = system.actorOf(
          Props(new Dispatcher(scrapers, null)), "dispatcher")

        val dep = DateTime.parse("2015-05-20T00:00:00Z")
        val ret = DateTime.parse("2015-07-20T00:00:00Z")
        val stayDays = new Duration(dep, ret).toStandardDays.getDays
        // create batch search criteria with fixed departure airport,
        // departure and return dates, and one possible destination
        // airport
        val criteria = BatchSearchCriteria(
          Set("ZAG"),
          Set("BRU"),
          dep,
          dep,
          ret,
          ret,
          stayDays,
          stayDays
        )

        val req = SearchRequest("user@mail.com", criteria, Some(Set("momondo", "qatar")))

        dispatcher ! req

        val params = SearchParams("ZAG", "BRU", dep, ret)

        momondo.expectMsg(1.seconds, params)
        qatar.expectMsg(1.seconds, params)
        airHr.expectNoMsg(1.seconds)
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
      sender ! (ps, Success(SearchYield(amount.toDouble, currency, "url")))
  }
}

