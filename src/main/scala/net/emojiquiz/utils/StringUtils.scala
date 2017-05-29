package net.emojiquiz.utils

import net.emojiquiz.model.Question
import org.slf4j.LoggerFactory


object StringUtils {

  val ValueSeparator = ","
  val Separator = ":::"
  val EmptyHelpers = List.empty[String]

  val log = LoggerFactory.getLogger(getClass)

  def processQuestionString(str: String): Option[Question] = {
    str.split(Separator) match {
      case Array(id, emojiString, answerString, help @ _*) =>
        val questionEmojis = emojiString.trim
        val answers = answerString.trim.split(ValueSeparator).map(_.toLowerCase).toList
        val helpEmojis = if (help.isEmpty) {
          EmptyHelpers
        } else {
          help(0).split(ValueSeparator).toList
        }
        Some(Question(id, "albums", questionEmojis, answers, helpEmojis))
      case _ =>
        print(s"Incorrect input $str")
        None
    }
  }

  // TODO: clean not alphanumeric
  def answerCorrect(userAnswer: String, correctAnswers: List[String]): Boolean = {
    correctAnswers.contains(userAnswer.toLowerCase)
  }

}
