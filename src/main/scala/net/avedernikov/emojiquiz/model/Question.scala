package net.avedernikov.emojiquiz.model

import com.typesafe.emoji.Emoji

/**
  * @author Artem Vedernikov
  */
case class Question(questionEmojis: List[Emoji], answers: List[String], helpEmojis: List[Emoji] = List.empty[Emoji]) {

  val questionString: String = questionEmojis.map(_.toString()).mkString("")

  lazy val helpersAvailable = helpEmojis.size

  def helpersString(helpersToPrint: Int): String = {
    helpEmojis.take(helpersToPrint).map(_.toString()).mkString("")
  }

}
