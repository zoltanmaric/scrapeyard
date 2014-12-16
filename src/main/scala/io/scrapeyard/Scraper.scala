package io.scrapeyard

import org.scalatest.selenium.Firefox
import org.scalatest.time.{Seconds, Span}

object Scraper extends Firefox {

  val host = "http://www.qatarairways.com"

  def doIt: Unit = {
    go to host
    println(pageTitle)

    implicitlyWait(Span(10, Seconds))

    Thread.sleep(5000)

    click on "book"
//    click on "book"
    Thread.sleep(2000)
    click on "FromTemp"
    Thread.sleep(2000)
//    click on "bc_from1"
//    enter("\t")
    enter("zagreb")
    Thread.sleep(2000)
    enter("\t")
    Thread.sleep(2000)
    enter("bali")
    Thread.sleep(2000)
    enter("\t")
  }
}