package io.scrapeyard

import javax.mail.search.FlagTerm
import javax.mail.{Flags, Folder, Session}

import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpecLike}
import akka.actor.ActorSystem
import spray.client.pipelining._


class ScheduleSearchSpec extends WordSpecLike with Matchers with Eventually {

  "single search" in {
    Boot.main(Array())

    val config = ConfigFactory.load("secret.properties")
    val (usr, pass) = (config.getString("username"), config.getString("password"))

    implicit val system = ActorSystem()
    implicit val execContext = scala.concurrent.ExecutionContext.global

    Thread.sleep(10000)

    val pipeline = sendReceive
    val responseFuture = pipeline {
      addHeader("Content-Type", "application/json")
      Post("http://localhost:8080/search",
        s"""
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
      """.stripMargin)
    }

    responseFuture onFailure {
      case t => fail(t)
    }

    val properties = System.getProperties
    val session = Session.getDefaultInstance(properties)
    val store = session.getStore("pop3")
    store.connect("pop.gmail.com", config.getString("username"), config.getString("password"))
    val inbox = store.getFolder("inbox")
    inbox.open(Folder.READ_ONLY)

    // search for all "unseen" messages
    val seen = new Flags(Flags.Flag.SEEN)
    val unseenFlagTerm = new FlagTerm(seen, false)

    eventually {
      val messages = inbox.search(unseenFlagTerm)
      messages.length should be > 0
    }


  }


}
