package io.scrapeyard

import akka.actor.Actor
import org.joda.time.DateTime
import spray.http.MediaTypes._
import spray.routing.HttpService

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class DispatcherServiceActor extends Actor with DispatcherService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(rootRoute)

  def dispatch: Unit = {
    val origs = Set("ZAG", "BUD")
    val dests = Set("DPS")
    val depFrom = DateTime.parse("2015-05-20T00:00:00.000Z")
    val depUntil = DateTime.parse("2015-05-22T00:00:00.000Z")
    val retFrom = DateTime.parse("2015-07-20T00:00:00.000Z")
    val retUntil = DateTime.parse("2015-07-31T00:00:00.000Z")
    val criteria = BatchSearchCriteria(origs, dests, depFrom, depUntil, retFrom, retUntil)
    val paramList = Dispatcher.toSearchParams(criteria)

    //  private val qatarFuture = Future {
    //    paramList.foreach { ps =>
    //      Try(QatarScraper.doIt(ps)) match {
    //        case Success(r) => println(r)
    //        case Failure(t) => println((ps, t))
    //      }
    //    }
    //  }

    val airHrFuture = Future {
      paramList.foreach { ps =>
        Try(AirHrScraper.doIt(ps)) match {
          case Success(r) => println(r)
          case Failure(t) => println((ps, t))
        }
      }
    }

    //  private val momondoFuture = Future {
    //    paramList.foreach { ps =>
    //      Try(MomondoScraper.doIt(ps)) match {
    //        case Success(r) => println(r)
    //        case Failure(t) => println((ps, t))
    //      }
    //    }
    //  }
    //  momondoFuture.onComplete(println(_))

    //  private val airHrFuture = Future {
    //    paramList.foreach { ps =>
    //      val res = Try(AirHrScraper.doIt(ps))
    //      println(res)
    //    }
    //  }
    //  airHrFuture.onComplete(println(_))

    //  Await.ready(qatarFuture, 1 hour)
    //  Await.ready(momondoFuture, 1 hour)
    Await.ready(airHrFuture, 1 hour)
  }
}

// this trait defines our service behavior independently from the service actor
trait DispatcherService extends HttpService {

  val rootRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      }
    }
}