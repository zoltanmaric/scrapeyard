package io.scrapeyard

import io.scrapeyard.Models.{SearchParams, SearchYield}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.scalatest.selenium.WebBrowser

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

// val ff = new FirefoxDriver with Firefox
class MomondoScraper extends Scraper with WebBrowser {

  implicit val webDriver = new SilentPhantomJSDriver()

  override protected def dateFormatter: DateTimeFormatter =
    DateTimeFormat.forPattern("dd-MM-yyyy")

  override def scrape(ps: SearchParams): Try[SearchYield] = Try {
    val StringSearchParams(org, dst, dep, ret) = toStringSearchParams(ps)
    val query = s"http://www.momondo.com/flightsearch/?Search=true&TripType=2&SegNo=2" +
      s"&SO0=$org&SD0=$dst&SDP0=$dep&SO1=$dst" +
      s"&SD1=$org&SDP1=$ret&AD=1&TK=ECO&DO=false&NA=false#Search=true&TripType=2&SegNo=2" +
      s"&SO0=$org&SD0=$dst&SDP0=$dep&SO1=$dst&SD1=$org&SDP1=$ret&AD=1&TK=ECO&DO=false&NA=false"
    go to query

    implicitlyWait(3 minutes)

    eventually (timeout(10 minutes), interval(200 millis)){
      find("searchProgressText").get.text should be ("Search complete")
    }

    val value = find(cssSelector("span[class=value]")).get.text.trim.replaceAll(",", "")
    val currency = find(cssSelector("span[class=unit]")).get.text.trim

    SearchYield(value.toDouble, currency, query)
  }
}