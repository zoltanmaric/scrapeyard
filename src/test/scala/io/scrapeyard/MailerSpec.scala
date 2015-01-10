package io.scrapeyard

import org.jvnet.mock_javamail.Mailbox
import org.scalatest.{Matchers, WordSpecLike}

class MailerSpec extends WordSpecLike with Matchers {

  "Mailer sends email" in {
    val to = "mock@mail.com"
    val subject = "Search results for scrapeyard.io"
    val body = "you want it to be one way"
    Mailer.sendMail(to, subject, body)

    val messages = Mailbox.get(to)

    messages.size should be (1)

    val msg = messages.get(0)
    msg.getFrom.head.toString should be ("no-reply@scrapeyard.io")
    msg.getAllRecipients.head.toString should be (to)
    msg.getSubject should be (subject)
    msg.getContent.toString should be (body)

  }
}
