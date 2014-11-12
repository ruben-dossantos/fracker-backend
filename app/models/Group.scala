package models

import akka.actor.{Actor, ActorLogging, Props, ActorRef}
import argonaut.Argonaut._
import argonaut.{CodecJson, Json}
import reactivemongo.bson.{BSONDocumentWriter, BSONDocument, BSONDocumentReader}

/**
 * Created by ruben on 10-11-2014.
 *
 */
case class Group(id: Int, name: String, password: String, users: List[String])

object Group {

  implicit def GroupCodecJson: CodecJson[Group] = casecodec4(Group.apply, Group.unapply)("id", "name", "password", "users")

  def toJson(group: Group) = group.asJson

  def parse(group: String): Either[String, Group] = group.decodeEither[Group].toEither

  implicit object GroupReader extends BSONDocumentReader[Group] {
    def read(doc: BSONDocument): Group = {
      val id = doc.getAs[Int]("id").get
      val name = doc.getAs[String]("name").get
      val password = doc.getAs[String]("password").get
      val users = doc.getAs[List[String]]("users").get
      Group(id, name, password, users)
    }
  }

  implicit object GroupWriter extends BSONDocumentWriter[Group]{
    def read(doc: BSONDocument): Group = {
      val id = doc.getAs[Int]("id").get
      val name = doc.getAs[String]("name").get
      val password = doc.getAs[String]("password").get
      val users = doc.getAs[List[String]]("users").get
      Group(id, name, password, users)
    }

    override def write(g: Group) = {
      BSONDocument(
        "id" -> g.id,
        "name" -> g.name,
        "password" -> g.password,
        "users" -> g.users
      )
    }
  }
}

object GroupActor {
  def props(group: ActorRef) = Props(classOf[GroupActor], group)
}

class GroupActor(group: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case json: String => createGroup(json)
  }

  def createGroup(json: String): Json = {
    Group.parse(json) match {
      case Right(new_group) => println(new_group)
      case Left(error) => println("Error decoding group")
    }
    Json()
  }

}
