package io.scrapeyard

import io.scrapeyard.Models.{SearchParams, SearchResult}
import org.joda.time.format.DateTimeFormat
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually

import scala.concurrent.duration._
import scala.language.postfixOps
// val ff = new FirefoxDriver with Firefox
object AirHrScraper extends SilentHtmlUnit with Matchers with Eventually {

  val host = "http://air.hr"

  def doIt(ps: SearchParams): SearchResult = {
    val fmt = DateTimeFormat.forPattern("dd.MM.yyyy")
    val org = ps.origin
    val dst = ps.destination
    val dep = fmt.print(ps.departure)
    val ret = fmt.print(ps.returning)
    val query = s"http://avio.air.hr/airhr/$org/$dst/$dep-$ret/1/0/0/rt"
    go to query
    println(pageTitle)

    implicitlyWait(3 minutes)

    eventually (timeout(3 minutes)){
      val p = find(cssSelector("div[class^=flight_price_v1] span[class=pull-right]")).get.text
      assert(!p.replaceAll("\\s", "").isEmpty)
    }

    val price = find(cssSelector("div[class^=flight_price_v1] span[class=pull-right]")).get.text

    SearchResult(ps, price, query)
  }
}