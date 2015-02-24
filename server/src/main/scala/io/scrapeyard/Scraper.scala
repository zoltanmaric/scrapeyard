package io.scrapeyard

import akka.actor.Actor
import io.scrapeyard.Models.{SearchParams, SearchYield}
import org.joda.time.format.DateTimeFormatter
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually

import scala.util.Try

class ScraperActor(scraper: Scraper) extends Actor {
  def receive = {
    case ps: SearchParams =>
      val triedYield = scraper.scrape(ps)
      sender ! (ps, triedYield)
  }
}

trait Scraper extends Matchers with Eventually {

  def scrape(ps: SearchParams): Try[SearchYield]

  protected def dateFormatter: DateTimeFormatter

  case class StringSearchParams(origin: String, destination: String, departure: String, returning: String)

  /**
   * Used to convert DateTime objects to strings based on the
   * scraper-specific DateTimeFormatter. Converting it to a
   * new case class enables simple extraction into separate values.
   */
  protected def toStringSearchParams(ps: SearchParams): StringSearchParams = {
    ps match {
      case SearchParams(org, dst, dep, ret) => StringSearchParams(
        org, dst, dateFormatter.print(dep), dateFormatter.print(ret))
    }
  }
}

class NonExistentConnectionException(msg: String) extends RuntimeException
