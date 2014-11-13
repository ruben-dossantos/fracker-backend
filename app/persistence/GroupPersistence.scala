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

  case class ReadGroup(id: Int)

  case class ReadGroups()

  case class UpdateGroup(id: String, group: Group)

  case class DeleteGroup(id: Int)

  case class FindGroup(name: String)

}

abstract class GroupPersistence extends Actor{

  def receive = {
    case cg: CreateGroup => sender ! createGroup(cg.group)
    case rgs: ReadGroups => sender ! readGroups
    case rg: ReadGroup => sender ! readGroup(rg.id)
    case ug: UpdateGroup => sender ! updateGroup(ug.id, ug.group)
    case dg: DeleteGroup => sender ! deleteGroup(dg.id)
    case fg: FindGroup => sender ! findGroup(fg.name)
  }

  def readGroups: List[Group] = { List() }

  def createGroup(group: Group): Option[BSONObjectID]

  def readGroup(id: Int): Option[Group]

  def updateGroup(id: String, group: Group): Boolean

  def deleteGroup(id: Int): Boolean

  def findGroup(name: String): Boolean

}
