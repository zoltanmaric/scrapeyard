package io.scrapeyard

import javax.mail.internet._
import courier._, Defaults._

import scala.concurrent.ExecutionContext.global

import akka.actor.Actor

object ScrapeMailer {

  case class SendEmail(to: String, subject: String, body: String)

  val From = "no-reply@scrapeyard.io"

  def sendMail(address: String, subject: String, body: String): Unit = {
    val mailer = Mailer("localhost", 25).auth(false)()

    mailer(Envelope.from(new InternetAddress(From))
      .to(new InternetAddress(address))
      .subject(subject)
      .content(Text(body)))
  }
}

class MailerActor extends Actor {
  import ScrapeMailer._

  def receive = {
    case SendEmail(to, subject, body) => sendMail(to, subject, body)
  }
}
