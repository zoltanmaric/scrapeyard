package io.scrapeyard


import javax.mail._
import javax.mail.internet._

import akka.actor.Actor

object Mailer {

  case class SendEmail(to: String, subject: String, body: String)

  val From = "no-reply@scrapeyard.io"

  def sendMail(address: String, subject: String, body: String): Unit = {
    // Set up the mail object
    val properties = System.getProperties
    properties.put("mail.smtp.host", "localhost")
    val session = Session.getDefaultInstance(properties)
    val message = new MimeMessage(session)

    // Set the from, to, subject, body text
    message.setFrom(new InternetAddress(From))
    message.setRecipients(Message.RecipientType.TO, address)
    message.setSubject(subject)
    message.setText(body)

    // And send it
    Transport.send(message)
  }
}

class MailerActor extends Actor {
  import Mailer._

  def receive = {
    case SendEmail(to, subject, body) => sendMail(to, subject, body)
  }
}
