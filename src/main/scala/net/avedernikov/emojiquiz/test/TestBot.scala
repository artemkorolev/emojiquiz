package net.avedernikov.emojiquiz.test

import java.util.concurrent.ConcurrentHashMap

import info.mukel.telegram.bots.api.{InputFile, ReplyKeyboardMarkup}
import info.mukel.telegram.bots.{Polling, TelegramBot}
import net.avedernikov.emojiquiz.utils.{CommandsWithDefault, EmojiUtils, Survey}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Artem Vedernikov
  */
class TestBot(token: String) extends TelegramBot(token) with Polling with CommandsWithDefault {

  import info.mukel.telegram.bots.OptionPimps._

  val log = LoggerFactory.getLogger(classOf[TestBot])

  val bkKorolevSurvey = new Survey("БК королев - это", List("Воля и разум", "Смирение", "Семья", "Команда, которая работает", "Лёха, Ден, Димка", "Команда, у которой нет потолка"))

  val answeringUsers = new ConcurrentHashMap[Int, Int]()


  on("start") { (sender, args) => Future {
      replyTo(sender) {
        args mkString " "
      }
    }
  }

  on("help") { (sender, args) => Future {
      replyTo(sender) {
        "hi"
      }
    }
  }

  // Return random emoji
  on("emoji") { (sender, args) => Future {
      replyTo(sender) {
        EmojiUtils.randomEmoji.toString()
      }
    }
  }

  on("bk_korolev") { (sender, args) => Future {
      sendPhoto(sender, InputFile("/Users/artem/Downloads/leha.jpg"), caption = "Надежда команды")
  }

  }

  on("survey") { (sender, args) => Future {
    println(args)
    args.toList match {
      case List("stat") =>
        replyTo(sender) {
          bkKorolevSurvey.prettyResults
        }
//      case "/answer" ::  =>

      case _ =>
        answeringUsers.put(sender, sender)
        sendMessage(sender,
          bkKorolevSurvey.question,
          replyMarkup = Some(ReplyKeyboardMarkup(bkKorolevSurvey.answers.map(Array(_)).toArray, oneTimeKeyboard = true)))
    }
  }

  }



  on("expensive_computation") { (sender, args) => Future {
    replyTo(sender) {
      // Expensive computation here
      Thread.sleep(10000)
      "42"
    }
  }}

  // Send a photo aysnchronously
  on("bender") { (sender, _) => Future(sendPhoto(sender, InputFile("./bender_photo.jpg"), caption = "Bender the great!!!"))}

  override def run(): Unit = {
    super.run()
    log.info("Starting successful")
  }

  override def onNonCommand(text: String, sender: Int): Unit = {
    println(s"Processing non command input $text")
    text match {
      case answer if bkKorolevSurvey.answers.contains(answer) && answeringUsers.containsKey(sender) =>
        bkKorolevSurvey.vote(answer)
        answeringUsers.remove(sender)
        replyTo(sender) {
          s"Voted \n ${bkKorolevSurvey.prettyResults}"
        }
      case _ =>
    }
  }

}