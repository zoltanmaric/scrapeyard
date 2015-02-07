package io.scrapeyard

import javax.mail.internet._
import com.typesafe.config.ConfigFactory
import courier._, Defaults._
import io.scrapeyard.Models.SearchResult

import scala.concurrent.ExecutionContext.global

import akka.actor.{ActorLogging, Actor}

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
  import ScrapeMailer._
  import spray.json._
  import ModelsJsonSupport._

  def receive = {
    case SendResults(to, subject, results) =>
      val body = results.toJson.prettyPrint
      log.debug(s"sending email: $to:\n$body")
      sendMail(to, subject, body)
  }
}
