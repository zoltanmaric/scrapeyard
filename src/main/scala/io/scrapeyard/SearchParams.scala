package io.scrapeyard

import org.joda.time.Instant

case class SearchParams(
                         origin: String,
                         destination: String,
                         departure: Instant,
                         returning: Instant
                         )
