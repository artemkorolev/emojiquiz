package net.avedernikov.emojiquiz.utils

import com.typesafe.emoji.Emoji
import net.avedernikov.emojiquiz.model.Question
import org.slf4j.LoggerFactory

/**
  * @author Artem Vedernikov
  */
object StringUtils {

  val AnswerSeparator = ","
  val Separator = ":::"
  val EmptyEmojis = List.empty[Emoji]

  val log = LoggerFactory.getLogger(getClass)

  def processQuestionString(str: String): Option[Question] = {
    str.split(Separator) match {
      case Array(emojiString, answerString, help @ _*) =>
        val emojis = readUnicodeEmojis(emojiString.trim)
        val answers = answerString.trim.split(AnswerSeparator).map(_.toLowerCase).toList
        val helpEmojis = if (help.isEmpty) {
          EmptyEmojis
        } else {
          readUnicodeEmojis(help(0).trim)
        }
        Some(Question(emojis, answers, helpEmojis))
      case _ =>
        print(s"Incorrect input $str")
        None
    }
  }

  def readUnicodeEmojis(str: String): List[Emoji] = {
    //scala reads emoji as 2 char symbols
    str.sliding(2, 2).map(e => Emoji(e.toCharArray)).toList
  }

  def answerCorrect(userAnswer: String, correctAnswers: List[String]): Boolean = {
    correctAnswers.contains(userAnswer.toLowerCase)
  }

}
