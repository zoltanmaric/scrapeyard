package io.scrapeyard

import org.joda.time.{Instant, DateTime}
import org.joda.time.format.DateTimeFormat
import spray.httpx.SprayJsonSupport
import spray.json._

object Models {
  case class BatchSearchCriteria(
                                origs: Set[String],
                                dests: Set[String],
                                depFrom: DateTime,
                                depUntil: DateTime,
                                retFrom: DateTime,
                                retUntil: DateTime
                                )

  case class SearchParams(
                           origin: String,
                           destination: String,
                           departure: Instant,
                           returning: Instant
                           )

  case class SearchResult(params: SearchParams, price: String, url: String)
}

object ModelsJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  import io.scrapeyard.Models.BatchSearchCriteria
  
  implicit object DateTimeJsonFormat extends RootJsonFormat[DateTime] {
    val Formatter = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC

    def write(dt: DateTime) = JsString(Formatter.print(dt))

    def read(value: JsValue) = value match {
      case JsString(v) => Formatter.parseDateTime(v)
      case v => deserializationError(s"Unable to interpret value '$v' as date")
    }
  }

  implicit val PortfolioFormats = jsonFormat6(BatchSearchCriteria)
}


