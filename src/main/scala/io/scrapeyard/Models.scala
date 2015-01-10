package io.scrapeyard

import io.scrapeyard.Models.SearchRequest
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
  
  case class SearchRequest(email: String, criteria: BatchSearchCriteria)

  case class SearchParams(
                           origin: String,
                           destination: String,
                           departure: Instant,
                           returning: Instant
                           )

  case class SearchResult(params: SearchParams, price: String, url: String)
  
  case class SearchResponse(email: String, results: Seq[SearchResult])
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

  implicit val BscFormat = jsonFormat6(BatchSearchCriteria)
  implicit val SearchReqFormat = jsonFormat2(SearchRequest)
}


