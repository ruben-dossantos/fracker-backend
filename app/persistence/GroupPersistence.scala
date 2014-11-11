package persistence

import akka.actor.Actor
import models.Group

/**
 * Created by ruben on 11-11-2014.
 *
 */
abstract class GroupPersistence extends Actor{

  def receive = {
    case id: Int => readGroup(id)
    case group: Group => createGroup(group)
  }

  def readGroups: List[Group] = { List() }

  def createGroup(group: Group): Option[Int]

  def readGroup(id: Int): Option[Group]

  def updateGroup(group: Group): Boolean

  def deleteGroup(id: Int): Boolean

}
