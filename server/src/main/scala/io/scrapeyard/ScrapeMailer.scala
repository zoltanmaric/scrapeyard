package io.scrapeyard

import javax.mail.internet._

import akka.actor.{Actor, ActorLogging}
import com.typesafe.config.ConfigFactory
import courier.Defaults._
import courier._
import io.scrapeyard.Models.SearchResult

object ScrapeMailer {

  val From = "no-reply@scrapeyard.io"

  val secretConf = ConfigFactory.load("secret.properties")

  def sendMail(address: String, subject: String, body: String): Unit = {
    val mailer = Mailer("smtp.gmail.com", 587)
      .auth(true)
      .as(secretConf.getString("username"), secretConf.getString("password"))
      .startTtls(true)()


    mailer(Envelope.from(new InternetAddress(From))
      .to(new InternetAddress(address))
      .subject(subject)
      .content(Text(body)))
  }
}

case class SendResults(to: String, subject: String, results: Set[SearchResult])

class MailerActor extends Actor with ActorLogging {
  import ModelsJsonSupport._
  import ScrapeMailer._
  import spray.json._

  def receive = {
    case SendResults(to, subject, results) =>
      val sortedResults = results.toVector.sortBy(r => (r.yld.currency, r.yld.value))
      val body = sortedResults.toJson.prettyPrint
      log.debug(s"sending email: $to:\n$body")
      sendMail(to, subject, body)
  }
}
