package persistence

import akka.actor.Actor
import models.{Users, User}
import persistence.UserPersistence._
import reactivemongo.bson.BSONObjectID

/**
 * Created by ruben on 11-11-2014.
 *
 */

object UserPersistence {

  case class CreateUser(user: User)

  case class ReadUser(id: Int)

  case class ReadUsers(name: Option[String])

  case class UpdateUser(id: String, user: User)

  case class DeleteUser(id: String)

  case class FindUser(username: String)

}

abstract class UserPersistence extends Actor {

  def receive = {
    case cu: CreateUser => sender ! createUser(cu.user)
    case rus: ReadUsers => sender ! readUsers(rus.name)
    case ru: ReadUser => sender ! readUser(ru.id)
    case uu: UpdateUser => sender ! updateUser(uu.id, uu.user)
    case du: DeleteUser => sender ! deleteUser(du.id)
    case fu: FindUser => sender ! findUser(fu.username)
  }

  def readUsers(name: Option[String]): Users = { Users(List()) }

  def createUser(user: User): Option[BSONObjectID]

  def readUser(id: Int): Option[User]

  def updateUser(id: String, user: User): Boolean

  def deleteUser(id: String): Boolean

  def findUser(username: String): Boolean
}
