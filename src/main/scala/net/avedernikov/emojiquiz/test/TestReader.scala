package net.avedernikov.emojiquiz.test

import scala.io.Source
import com.typesafe.emoji.Emoji
import com.typesafe.emoji.ShortCodes.Implicits._
import com.typesafe.emoji.ShortCodes.Defaults._

/**
  * @author Artem Vedernikov
  */
object TestReader extends App {

  //println("cat".emoji.toString())


  val filePath = "/Users/artem/x.txt"

  val file = Source.fromFile(filePath).getLines().toList

//  file.foreach { x =>
//    //scala reads emoji as 2 char symbols
//    val emojiString = x.sliding(2, 2).map(e => Emoji(e.toCharArray).name).toList
//
//    println(emojiString)
//
//  }


  val str = "a b "

  str.split(" ") match {
    case Array(a, b, c) =>
      println(c)
    case _ =>
      println("xxx")
  }

  val x = "ship".emoji

  println(x.toHexString)
}
