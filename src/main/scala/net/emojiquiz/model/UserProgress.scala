package net.emojiquiz.model

import com.mongodb.{DBObject, WriteConcern}
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.MongoDBObject
import net.emojiquiz.utils.MongoUtils


// todo: refactor current question
case class UserProgress(_id: Long,
                        currentQuestion: Option[Question],
                        level: Int = 0,
                        score: Int = 0,
                        hintsUsed: Int = 0,
                        totalHintsUsed: Int = 0) {

  val GameFinished = "Game finished"

  val canUseHints: Boolean = {
    currentQuestion.exists(hintsUsed < _.hintsAvailable)
  }

  lazy val toPrettyString: String = {

    val maybeQuestion = currentQuestion.map {
      q =>
        s"""
           |Question: ${q.questionEmojis}
           |Topic: ${q.topic}
         """.stripMargin
    }.getOrElse(GameFinished)

    val maybeHintsUsed = currentQuestion.flatMap(_.hintsString(hintsUsed)).getOrElse("")

    s"""
       |$maybeQuestion
       |Level: $level
       |Score: $score
       |Hints used: $hintsUsed $maybeHintsUsed
       |Total hints used: $totalHintsUsed
     """.stripMargin
  }

  def toDbObject: DBObject = {
    val x = MongoDBObject("_id" -> _id, "level" -> level, "score" -> score, "hintsUsed" -> hintsUsed, "totalHintsUsed" -> totalHintsUsed)
    currentQuestion.foreach(q => x.put("currentQuestion", q.toDBObject))
    x
  }
}

object UserProgress {

  def apply(dbObject: DBObject): UserProgress = {
    val id = dbObject.get("_id").toString.toLong
    val maybeQuestion = Option(dbObject.get("currentQuestion").asInstanceOf[DBObject]).map(Question.apply)
    val level = dbObject.get("level").toString.toInt
    val score = dbObject.get("score").toString.toInt
    val hintsUsed = dbObject.get("hintsUsed").toString.toInt
    val totalHintsUsed = dbObject.get("totalHintsUsed").toString.toInt

    UserProgress(id, maybeQuestion, level, score, hintsUsed, totalHintsUsed)
  }
}

object UserProgressDAO {

  val Collection = "user_progress"

  def apply(mongoHosts: String,
            dbName: String = CommonDAO.Db,
            collectionName: String = Collection,
            user: Option[String] = None,
            password: Option[String] = None) = {
    val mongoCollection = MongoUtils.getCollection(mongoHosts, dbName, collectionName, user, password)
    new UserProgressDAO(mongoCollection)
  }

}

class UserProgressDAO(mongoCollection: MongoCollection) {

  def findOne(query: DBObject): Option[UserProgress] = {
    mongoCollection.findOne(query).map(UserProgress.apply)
  }


  def removeById(id: Long): Unit = {
    mongoCollection.remove(MongoDBObject("_id" -> id))
  }

  def update(query: DBObject, updateFields: DBObject, upsert: Boolean, multi: Boolean, writeConcern: WriteConcern): Unit = {
    mongoCollection.update(query, updateFields, upsert, multi, writeConcern)
  }

  def save(userProgress: UserProgress): Unit = {
    mongoCollection.save(userProgress.toDbObject)
  }

}