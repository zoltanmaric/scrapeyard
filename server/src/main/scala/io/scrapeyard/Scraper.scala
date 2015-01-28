package io.scrapeyard

import io.scrapeyard.Models.{SearchResult, SearchParams}
import org.joda.time.format.DateTimeFormatter
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually

import scala.util.Try

trait Scraper extends Matchers with Eventually {

  def scrape(ps: SearchParams): Try[SearchResult]

  protected def dateFormatter: DateTimeFormatter

  case class StringSearchParams(origin: String, destination: String, departure: String, returning: String)

  protected def toStringSearchParams(ps: SearchParams): StringSearchParams = {
    ps match {
      case SearchParams(org, dst, dep, ret) => StringSearchParams(
        org, dst, dateFormatter.print(dep), dateFormatter.print(ret))
    }
  }
}
