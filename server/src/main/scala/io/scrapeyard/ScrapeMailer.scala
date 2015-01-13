package io.scrapeyard

import javax.mail.internet._
import com.typesafe.config.ConfigFactory
import courier._, Defaults._

import scala.concurrent.ExecutionContext.global

import akka.actor.{ActorLogging, Actor}

object ScrapeMailer {

  case class SendEmail(to: String, subject: String, body: String)

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

class MailerActor extends Actor with ActorLogging {
  import ScrapeMailer._

  def receive = {
    case SendEmail(to, subject, body) =>
      log.debug(s"sending email: $to:\n$body")
      sendMail(to, subject, body)
  }
}
