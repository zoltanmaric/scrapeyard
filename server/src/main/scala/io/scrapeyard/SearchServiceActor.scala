package io.scrapeyard

import akka.actor.{Actor, ActorRef, Props}
import io.scrapeyard.Models.SearchRequest
import spray.http.MediaTypes._
import spray.routing.HttpService

class SearchServiceActor extends Actor with SearchService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  lazy val scrapers: Set[ActorRef] = {
    val scraperProps = Map(
      "airHrScraper" -> Props(new ScraperActor(AirHrScraper)),
      "momondoScraper" -> Props(new ScraperActor(MomondoScraper)),
      "qatarScraper" -> Props(new ScraperActor(QatarScraper))
    )

    scraperProps.map {
      case (name, props) => context.actorOf(props, name)
    }.toSet
  }

  val mailerProps = Props[MailerActor]

  def createDispatcher = context.actorOf(Props(new Dispatcher(scrapers, mailerProps)))

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(searchRoute)
}

// this trait defines our service behavior independently from the service actor
trait SearchService extends HttpService {

  import io.scrapeyard.ModelsJsonSupport._

  def createDispatcher: ActorRef

  val searchRoute =
    path("search") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      } ~
      post {
        respondWithMediaType(`application/json`)
        entity(as[SearchRequest]) { req =>
          val dispatcher = createDispatcher
          dispatcher ! req
          println(req)
          complete(req)
        }
      }
    }
}