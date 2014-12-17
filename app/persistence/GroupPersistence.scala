package persistence

import akka.actor.Actor
import models.Group
import persistence.GroupPersistence._
import reactivemongo.bson.BSONObjectID

/**
 * Created by ruben on 11-11-2014.
 *
 */
object GroupPersistence {

  case class CreateGroup(group: Group)

  case class ReadGroup(id: String)

  case class ReadGroups(username: Option[String])

  case class UpdateGroup(id: String, group: Group)

  case class DeleteGroup(id: String)

  case class FindGroup(name: String)

}

abstract class GroupPersistence extends Actor{

  def receive = {
    case cg: CreateGroup => sender ! createGroup(cg.group)
    //case rgs: ReadGroups => sender ! readGroups(rgs.username)
    case rg: ReadGroup => sender ! readGroup(rg.id)
    case ug: UpdateGroup => sender ! updateGroup(ug.id, ug.group)
    case dg: DeleteGroup => sender ! deleteGroup(dg.id)
    case fg: FindGroup => sender ! findGroup(fg.name)
  }

  //def readGroups(username: Option[String]): Groups = { Groups(List()) }

  def createGroup(group: Group): Option[BSONObjectID]

  def readGroup(id: String): Option[Group]

  def updateGroup(id: String, group: Group): Boolean

  def deleteGroup(id: String): Boolean

  def findGroup(name: String): Boolean

}
