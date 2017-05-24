package net.emojiquiz.utils

import org.scalatest.{FunSuite, Matchers}

/**
  * @author Artem Vedernikov
  */
class StringUtilsTest extends FunSuite with Matchers {

  val testEmojiString = "\uD83D\uDC73\uD83C\uDF0A\uD83D\uDCD6"

  val helperEmojiString ="\uD83D\uDCD6"

  val correctAnswers = List("nemo", "cptn nemo")

  test("StringUtils should check if answer is correct #1") {
    StringUtils.answerCorrect(correctAnswers(0), correctAnswers) should be(true)
    StringUtils.answerCorrect(correctAnswers(1).toUpperCase, correctAnswers) should be(true)
    StringUtils.answerCorrect("nautilus", correctAnswers) should be(false)
  }

}
