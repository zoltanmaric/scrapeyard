package io.scrapeyard


import javax.mail._
import javax.mail.internet._

object Mailer {
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
