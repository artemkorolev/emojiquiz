package net.avedernikov.emojiquiz

import net.avedernikov.emojiquiz.bots.QuizBot
import net.avedernikov.emojiquiz.test.TestBot
import net.avedernikov.emojiquiz.utils.StringUtils
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.util.Try

/**
  * @author Artem Vedernikov
  */
object Runner extends App {

  val log = LoggerFactory.getLogger(getClass)

  // for heroku
  val TelegramTokenVariable = "TELEGRAM_TOKEN"
  val token = Try(sys.env(TelegramTokenVariable)).getOrElse("190060341:AAElYY1cAVGXmI28ifO7V_3SmVIAKl9rad4")

  val questionsFile = "/Users/artem/repo/emojiquiz/questions.txt"

  val questions = Source.fromFile(questionsFile).getLines().flatMap(StringUtils.processQuestionString).toList
  println(questions)
  log.info("Loaded questions {}", questionsFile)

  val bot = new QuizBot(token, questions)
  bot.run()

}
