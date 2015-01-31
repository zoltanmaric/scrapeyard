package io.scrapeyard

import java.util.logging.Level

import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.scalatest.selenium.HtmlUnit

class SilentPhantomJSDriver extends PhantomJSDriver {
  // PhantomJSDriver prints error when an element has not yet appeared,
  // so we silence it.
  java.util.logging.Logger.getLogger("org.apache.http").setLevel(Level.WARNING)
  java.util.logging.Logger.getLogger("org.openqa.selenium.phantomjs").setLevel(Level.WARNING)
}
