package net.emojiquiz

import com.typesafe.config.ConfigFactory
import net.emojiquiz.bot.EmojiQuizBot
import net.emojiquiz.model.{QuestionDAO, UserProgressDAO}
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.util.Try

object Runner extends App {

  // for heroku
  val TelegramTokenVariable = "TELEGRAM_TOKEN"
  val questionsFile = "/Users/artem/repo/emojiquiz/questions.txt"
  val CommentCharacter = "#"

  val log = LoggerFactory.getLogger(getClass)
  val config = ConfigFactory.load()

  val token = Try(sys.env(TelegramTokenVariable)).getOrElse {
    log.info(s"No sys variable $TelegramTokenVariable found, using config")
    val tokenPath = config.getString("token.path")
    Source.fromResource(tokenPath).getLines().toList.head
  }

  val mongoHost = config.getString("mongo.host")
  val mongoUser = Try(config.getString("mongo.user")).toOption
  val mongoPassword = Try(config.getString("mongo.password")).toOption


  val questionDao = QuestionDAO(mongoHost, user = mongoUser, password = mongoPassword)

 // val questions = Source.fromFile(questionsFile).getLines().filterNot(_.startsWith(CommentCharacter)).flatMap(StringUtils.processQuestionString).toList

//  questions.foreach(questionDao.save)

  val userProgressDao = UserProgressDAO(mongoHost, user = mongoUser, password = mongoPassword)

  val bot = new EmojiQuizBot(token, questionDao, userProgressDao)
  bot.run()

  sys.addShutdownHook(bot.shutdown())

}
