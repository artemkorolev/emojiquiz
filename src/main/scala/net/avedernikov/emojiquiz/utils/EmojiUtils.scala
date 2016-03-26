package net.avedernikov.emojiquiz.utils

import com.typesafe.emoji.{Emoji, ShortCodes}

import scala.util.Random

/**
  * @author Artem Vedernikov
  */
object EmojiUtils {

  val Emojis = ShortCodes.Defaults.defaultImplicit.emojis
  val EmojisList = Emojis.toList

  def randomEmoji: Emoji = EmojisList(Random.nextInt(EmojisList.size))

}
