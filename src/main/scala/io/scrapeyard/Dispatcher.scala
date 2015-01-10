package io.scrapeyard

import akka.actor.{Actor, ActorLogging}
import io.scrapeyard.Models.{BatchSearchCriteria, SearchParams, SearchRequest}

import scala.language.postfixOps
import scala.util.{Failure, Try}

class Dispatcher extends Actor with ActorLogging {

  import io.scrapeyard.Dispatcher._

  def receive: Receive = {
    case req: SearchRequest => dispatch(req)
  }

  def dispatch(req: SearchRequest) = {
    val paramList = toSearchParams(req.criteria)

    val searchTries = paramList map { ps => Try(AirHrScraper.doIt(ps)) }

    val (succs, fails) = searchTries.partition(_.isSuccess)

    // TODO: send email
    val results = succs map (_.get)
    log.info("Successful search results:")
    results.foreach(r => log.info(r.toString))

    if (fails.nonEmpty) {
      log.warning("Failed searches: ")
      fails.foreach {
        case Failure(t) => log.error(t, t.getMessage)
      }
    }
  }
}

object Dispatcher {
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
    } yield SearchParams(orig, dest, dep.toInstant, ret.toInstant)

    searches.toVector
  }
}
