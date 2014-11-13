package persistence

import akka.actor.Actor
import models.User
import persistence.UserPersistence._
import reactivemongo.bson.BSONObjectID

/**
 * Created by ruben on 11-11-2014.
 *
 */

object UserPersistence {

  case class CreateUser(user: User)

  case class ReadUser(id: Int)

  case class ReadUsers()

  case class UpdateUser(id: Int, user: User)

  case class DeleteUser(id: Int)

  case class FindUser(username: String)

}

abstract class UserPersistence extends Actor {

  def receive = {
    case cu: CreateUser => sender ! createUser(cu.user)
    case rus: ReadUsers => sender ! readUsers
    case ru: ReadUser => sender ! readUser(ru.id)
    case uu: UpdateUser => sender ! updateUser(uu.id, uu.user)
    case du: DeleteUser => sender ! deleteUser(du.id)
    case fu: FindUser => sender ! findUser(fu.username)
  }

  def readUsers: List[User] = { List() }

  def createUser(user: User): Option[BSONObjectID]

  def readUser(id: Int): Option[User]

  def updateUser(id: Int, user: User): Boolean

  def deleteUser(id: Int): Boolean

  def findUser(username: String): Boolean
}
