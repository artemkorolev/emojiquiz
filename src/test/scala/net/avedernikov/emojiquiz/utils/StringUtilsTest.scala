package net.avedernikov.emojiquiz.utils

import org.scalatest.{FunSuite, Matchers}

/**
  * @author Artem Vedernikov
  */
class StringUtilsTest extends FunSuite with Matchers {

  val testEmojiString = "\uD83D\uDC73\uD83C\uDF0A\uD83D\uDCD6"

  val helperEmojiString ="\uD83D\uDCD6"

  val correctAnswers = List("nemo", "cptn nemo")

  test("StringUtils should correctly read emoji from string") {
    val result = StringUtils.readUnicodeEmojis(testEmojiString).map(_.name)
    result should equal(List("MAN WITH TURBAN", "WATER WAVE", "OPEN BOOK"))
  }

  test("StringUtils should correct read emoji question with answer from string") {
    val questonWithAnswer = testEmojiString + StringUtils.Separator + "nemo" + StringUtils.AnswerSeparator + "cptn nemo"
    val resultOpt = StringUtils.processQuestionString(questonWithAnswer)

    resultOpt should be('defined)
    val result = resultOpt.get

    result.questionEmojis.map(_.name) should equal(List("MAN WITH TURBAN", "WATER WAVE", "OPEN BOOK"))
    result.answers should equal(List("nemo", "cptn nemo"))
    result.helpEmojis should be('empty)
  }

  test("StringUtils should correct read emoji question with answer and helpers from string") {
    val questonWithAnswer = testEmojiString +
      StringUtils.Separator +
      "nemo" +
      StringUtils.AnswerSeparator +
      "cptn nemo" +
      StringUtils.Separator +
      helperEmojiString

    val resultOpt = StringUtils.processQuestionString(questonWithAnswer)

    resultOpt should be('defined)
    val result = resultOpt.get
    result.questionEmojis.map(_.name) should equal(List("MAN WITH TURBAN", "WATER WAVE", "OPEN BOOK"))
    result.answers should equal(List("nemo", "cptn nemo"))
    result.helpEmojis.map(_.name) should equal(List("OPEN BOOK"))
  }

  test("StringUtils should check if answer is correct #1") {
    StringUtils.answerCorrect(correctAnswers(0), correctAnswers) should be(true)
    StringUtils.answerCorrect(correctAnswers(1).toUpperCase, correctAnswers) should be(true)
    StringUtils.answerCorrect("nautilus", correctAnswers) should be(false)
  }

}
