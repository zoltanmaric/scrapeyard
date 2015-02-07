package io.scrapeyard

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import io.scrapeyard.Models._
import io.scrapeyard.ScrapeController.ControllerReq
import org.joda.time.DateTime
import org.scalatest.WordSpecLike

import scala.concurrent.duration._
import scala.util.Success

class ScrapeControllerActorTest extends WordSpecLike {
  implicit val system = ActorSystem("TestSys")

  class StepParent(childProps: Props, probe: ActorRef) extends Actor {
    val child = context.actorOf(childProps, "child")
    def receive = {
      case msg if sender == child =>
        probe forward msg
      case msg =>
        child forward msg
    }
  }

  "receive a search request and return results" in {
    val dispatcher = TestProbe()
    val controller = system.actorOf(Props(new StepParent(Props[ScrapeControllerActor], dispatcher.ref)), "stepparent")
    val scraper = TestProbe()

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

    dispatcher.send(controller, ControllerReq(criteria, scraper.ref))

    val params1 = SearchParams("ZAG", "BRU", dep, ret)
    val params2 = SearchParams("ZAG", "OST", dep, ret)
    scraper.expectMsgAllOf(2.seconds, params1, params2)

    val yld1 = SearchYield("2 USD", "url1")
    val yld2 = SearchYield("4 USD", "url2")
    scraper.send(controller, (params1, Success(yld1)))
    scraper.send(controller, (params2, Success(yld2)))

    val results = Set(
      SearchResult(params1, yld1),
      SearchResult(params2, yld2)
    )

    dispatcher.expectMsg(2.seconds, results)

    system.shutdown()
  }
}
