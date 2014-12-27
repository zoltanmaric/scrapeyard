package io.scrapeyard

import org.joda.time.DateTime

case class BatchSearchCriteria(
                              origs: Set[String],
                              dests: Set[String],
                              depFrom: DateTime,
                              depUntil: DateTime,
                              retFrom: DateTime,
                              retUntil: DateTime
                                )
