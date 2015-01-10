package io.scrapeyard

import akka.actor.Actor
import io.scrapeyard.Models.{SearchParams, BatchSearchCriteria}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import language.postfixOps

import scala.concurrent.ExecutionContext.Implicits.global

class Dispatcher extends Actor {

  import Dispatcher._

  def receive: Receive = {
    case bsc: BatchSearchCriteria => dispatch(bsc)
  }

  def dispatch(criteria: BatchSearchCriteria) = {
    val paramList = toSearchParams(criteria)

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
