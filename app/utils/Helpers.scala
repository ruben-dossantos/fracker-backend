package utils

import argonaut.Argonaut._
import argonaut.Json
import reactivemongo.bson.BSONObjectID

/**
 * Created by ruben on 13-11-2014.
 *
 */
object Helpers {

  case class POST(json: String)

  case class GET(id: String)

  case class GETS(name: Option[String], username: Option[String])

  case class PUT(id: String, json: String)

  case class DELETE(id: String)

  def verify_id(id: Option[String]):Option[BSONObjectID] = {
    id match {
      case None => None
      case Some(x) => Some(BSONObjectID(x))
    }
  }

  def jsonThrowable(text: String) = new Throwable(Json("error" -> jString(text)).toString())
}
