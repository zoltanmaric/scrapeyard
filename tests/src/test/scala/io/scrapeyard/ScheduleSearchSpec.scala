package io.scrapeyard

import javax.mail.search.FlagTerm
import javax.mail.{Flags, Folder, Session}

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpecLike}
import spray.client.pipelining._
import spray.http.{ContentTypes, HttpEntity}

import scala.concurrent.duration._
import scala.language.postfixOps


class ScheduleSearchSpec extends WordSpecLike with Matchers with Eventually {

  "single search" in {
    Boot.main(Array())

    val config = ConfigFactory.load("secret.properties")
    val (usr, pass) = (config.getString("username"), config.getString("password"))

    implicit val system = ActorSystem()
    implicit val execContext = scala.concurrent.ExecutionContext.global

    // Wait for the server to start
    Thread.sleep(10000)

    val uri = "http://localhost:8080/search"
    val content = s"""
        |{
        | "email": "$usr@gmail.com",
        | "criteria": {
        |  "origs": ["ZAG"],
        |  "dests": ["DPS"],
        |  "depFrom": "2015-07-17",
        |  "depUntil": "2015-07-17",
        |  "retFrom": "2015-07-29",
        |  "retUntil": "2015-07-29"
        | }
        |}
      """.stripMargin
    val pipeline = sendReceive
    val entity = HttpEntity(ContentTypes.`application/json`, content)
    val req = Post(uri, entity)
    val responseFuture = pipeline(req)

    responseFuture onFailure {
      case t => fail(t)
    }

    val session = Session.getDefaultInstance(System.getProperties)
    val store = session.getStore("imaps")
    store.connect("imap.gmail.com", usr, pass)

    // search for all "unseen" messages
    val seen = new Flags(Flags.Flag.SEEN)
    val unseenFlagTerm = new FlagTerm(seen, false)
    val inbox = store.getFolder("inbox")

    eventually(timeout(10 minutes)) {
      inbox.open(Folder.READ_WRITE)
      val messages = inbox.search(unseenFlagTerm)
      // if any messages found, mark them read
      inbox.setFlags(messages, seen, true)
      inbox.close(false)

      messages.length should be > 0
    }
  }


}
