package io.scrapeyard

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import spray.httpx.SprayJsonSupport
import spray.json._

case class BatchSearchCriteria(
                              origs: Set[String],
                              dests: Set[String],
                              depFrom: DateTime,
                              depUntil: DateTime,
                              retFrom: DateTime,
                              retUntil: DateTime
                                )

object BatchSearchCriteriaJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object DateTimeJsonFormat extends RootJsonFormat[DateTime] {
    val Formatter = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC

    def write(dt: DateTime) = JsString(Formatter.print(dt))

    def read(value: JsValue) = value match {
      case JsString(v) => Formatter.parseDateTime(v)
    }
  }

  implicit val PortofolioFormats = jsonFormat6(BatchSearchCriteria)
}


