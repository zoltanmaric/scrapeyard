package io.scrapeyard

import io.scrapeyard.Models.{SearchParams, SearchYield}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.scalatest.selenium.WebBrowser

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

class AirHrScraper extends Scraper with WebBrowser {

  implicit lazy val webDriver = new SilentPhantomJSDriver()

  override def scrape(ps: SearchParams): Try[SearchYield] = Try {
    val StringSearchParams(org, dst, dep, ret) = toStringSearchParams(ps)
    val query = s"http://avio.air.hr/airhr/$org/$dst/$dep-$ret/1/0/0/rt"
    go to query

    implicitlyWait(3 minutes)

    eventually (timeout(3 minutes), interval(200 millis)){
      val p = find(cssSelector("div[class^=flight_price_v1] span[class=pull-right]")).get.text
      assert(!p.replaceAll("\\s", "").isEmpty)
    }

    val price = find(cssSelector("div[class^=flight_price_v1] span[class=pull-right]")).get.text
      .trim.split("\\s")
    val (value, currency) = (price(0), price(1))

    SearchYield(value.toDouble, currency, query)
  }

  override protected def dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern("dd.MM.yyyy")
}