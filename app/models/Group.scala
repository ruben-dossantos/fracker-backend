package models

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import argonaut.Argonaut._
import argonaut.{CodecJson, DecodeJson, EncodeJson, Json}
import persistence.GroupPersistence._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}
import utils.Helpers._
import utils.ActorUtils._

/**
 * Created by ruben on 10-11-2014.
 *
 */
case class Group(_id: Option[BSONObjectID], name: String, password: String, users: List[String])      // TODO: this needs to have more info without recursively take one another

case class Groups(groups: List[Group]) {
  implicit def GroupsCodecJson: CodecJson[Groups] = casecodec1(Groups.apply, Groups.unapply)("groups")

  def toJson = this.groups.asJson
}

object Group {

  implicit def GroupEncodeJson: EncodeJson[Group] = EncodeJson( (g: Group) => ("_id" := g._id.get.toString()) ->: ("name" := g.name) ->: ("password" := g.password) ->: ("users" := g.users) ->: jEmptyObject)

  implicit def GroupDecodeJson: DecodeJson[Group] = DecodeJson( g => for {
    _id <- (g --\ "_id").as[Option[String]]
    name <- (g --\ "name").as[String]
    password <- (g --\ "password").as[String]
    users <- (g --\ "users").as[List[String]]
  } yield Group(verify_id(_id), name, password, users))

  def toJson(group: Group) = group.asJson

  def parse(group: String): Either[String, Group] = group.decodeEither[Group].toEither

  implicit object GroupReader extends BSONDocumentReader[Group] {
    def read(doc: BSONDocument): Group = {
      val _id = doc.getAs[BSONObjectID]("_id").get
      val name = doc.getAs[String]("name").get
      val password = doc.getAs[String]("password").get
      val users = doc.getAs[List[String]]("users").get
      Group(Some(_id), name, password, users)
    }
  }

  implicit object GroupWriter extends BSONDocumentWriter[Group]{
    def read(doc: BSONDocument): Group = {
      val _id = doc.getAs[BSONObjectID]("_id").get
      val name = doc.getAs[String]("name").get
      val password = doc.getAs[String]("password").get
      val users = doc.getAs[List[String]]("users").get
      Group(Some(_id), name, password, users)
    }

    override def write(g: Group) = {
      BSONDocument(
        "_id" -> g._id,
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
    case post: POST => sender ! createGroup(post.json)
    case g: GET => sender ! getGroup(g.id)
    case g: GETS => sender ! getGroups(g.username)
    case d: DELETE => sender ! deleteGroup(d.id)
  }

  def createGroup(json: String): Json = {
    Group.parse(json) match {
      case Right(new_group) =>
        await[Boolean](group, FindGroup(new_group.name)) match {
          case false =>
            await[Option[BSONObjectID]](group, CreateGroup(new_group)) match {
              case Some(id) => Json("_id" -> jString(id.stringify))
              case None => Json("error" -> jString("Error creating group in the database"))
            }
          case true => Json("error" -> jString("group with that name already exists"))
        }

      case Left(error) => Json("error" -> jString("Error decoding json group"))
    }
  }

  def getGroup(id: String): Json = {
    await[Option[Group]](group, ReadGroup(id)) match {
      case Some(this_group) => Group.toJson(this_group)
      case None => Json("error" -> jString("Group not found"))
    }
  }

  def getGroups(username: Option[String]): Json = await[Groups](group, ReadGroups(username)).toJson

  def deleteGroup(id: String): Boolean = await[Boolean](group, DeleteGroup(id))
}
