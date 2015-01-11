package io.scrapeyard

import java.util.logging.Level

import org.scalatest.selenium.HtmlUnit

trait SilentHtmlUnit extends HtmlUnit {
  // HtmlUnit throws all kinds of error logs when an element has not yet appeared,
  // so we silence it. FORECFULLY! GGGAAAAAHH!!
  java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
}
