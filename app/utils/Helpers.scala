package utils

import reactivemongo.bson.BSONObjectID

/**
 * Created by ruben on 13-11-2014.
 *
 */
object Helpers {

  case class POST(json: String)

  case class GET(id: Int)

  case class GETS()

  case class PUT(id: Int, json: String)

  case class DELETE(id: Int)

  def verify_id(id: Option[String]):Option[BSONObjectID] = {
    id match {
      case None => None
      case Some(x) => Some(BSONObjectID(x))
    }
  }
}
