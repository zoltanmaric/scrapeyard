package io.scrapeyard

object Main extends App {
  println("hello world")

  val arrivals = Seq(
    "25-Jul-2015",
    "26-Jul-2015",
    "27-Jul-2015",
    "28-Jul-2015",
    "29-Jul-2015",
    "30-Jul-2015",
    "31-Jul-2015",
    "01-Aug-2015",
    "02-Aug-2015",
    "03-Aug-2015",
    "04-Aug-2015",
    "05-Aug-2015",
    "06-Aug-2015",
    "07-Aug-2015",
    "08-Aug-2015",
    "09-Aug-2015",
    "10-Aug-2015"
  )

  val arrivalsToPrices = arrivals.map { a =>
    val pair = (a, Scraper.doIt(a))
    println(pair)
    pair
  }.toMap

  println(arrivalsToPrices.mkString("\n"))
}