package persistence

import akka.actor.Actor
import models.User

/**
 * Created by ruben on 11-11-2014.
 *
 */
abstract class UserPersistence extends Actor {

  def receive = {
    case id: Int => readUser(id)
    case user: User => createUser(user)
  }

  def readUsers: List[User] = { List() }

  def createUser(user: User): Option[Int]

  def readUser(id: Int): Option[User]

  def updateUser(user: User): Boolean

  def deleteUser(id: Int): Boolean
}
