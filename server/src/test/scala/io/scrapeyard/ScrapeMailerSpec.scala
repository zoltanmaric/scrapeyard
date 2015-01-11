package io.scrapeyard

import org.jvnet.mock_javamail.Mailbox
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

class ScrapeMailerSpec extends WordSpecLike with Matchers with Eventually {
    "send an email" in {
      val to = "mock@mail.com"
      val subject = "Search results for scrapeyard.io"
      val body = "you want it to be one way"
      ScrapeMailer.sendMail(to, subject, body)

      eventually(timeout(2 seconds)) {
        val inbox = Mailbox.get(to)
        inbox.size should be (1)
        val msg = inbox.get(0)
        msg.getSubject should be (subject)
        msg.getContent should be (body)
      }
    }
}
