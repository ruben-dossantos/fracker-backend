package persistence

import akka.actor.{ActorLogging, Props}
import models.User
import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
 * Created by ruben on 11-11-2014.
 *
 */
object UserMongoPersistence {
  def props(db_name: String = "fracker", collection_name: String = "users"): Props = {
    Props(classOf[UserMongoPersistence], db_name, collection_name)
  }
}

class UserMongoPersistence (db_name: String, collection_name: String) extends UserPersistence with ActorLogging{

  var connection = MongoDriver().connection(Seq("localhost"))
  var db = connection.db(db_name)
  var users = db.collection[BSONCollection](collection_name)

  def withMongoConnection[T](body: => T): Try[T] = {
    Try{
      if(users == null){
        connection = MongoDriver().connection(Seq("localhost"))
        db = connection.db(db_name)
        users.indexesManager.ensure(Index(List(("_id", Ascending)), unique = true))
        users.indexesManager.ensure(Index(List(("name", Ascending)), unique = true))
      }

      body
    }
  }

  override def createUser(user: User): Option[BSONObjectID] = {
    val new_user = User(Some(BSONObjectID.generate), user.username, user.first_name, user.last_name, user.password, user.lat, user.lon, user.timestamp)
    withMongoConnection {
      Await.result(
        users.insert(new_user).map {
          lastError =>
            lastError.ok match {
              case true => new_user._id
              case false => throw new Exception(lastError)
            }
        }, 5.seconds
      )
    } match {
      case Success(id) => id //TODO whats new_user
      case Failure(_) => None
    }
  }

  override def deleteUser(id: Int): Boolean = {
    withMongoConnection {
      users.remove(BSONDocument("id" -> id))
    } match {
      case Success(_) => true
      case Failure(_) => false
    }
  }

  override def updateUser(id: Int, user: User): Boolean = ???

  override def readUser(id: Int): Option[User] = {
    withMongoConnection {
      val query = BSONDocument("id" -> id)
      Await.result(users.find(query).one[User], 5.seconds)
    } match {
      case Success(user) => user
      case Failure(_) => None
    }
  }

  override def findUser(username: String): Boolean = {
    withMongoConnection {
      val query = BSONDocument("username" -> username)
      Await.result(users.find(query).one[User], 5.seconds)
    } match {
      case Success(user) =>
        user match {
          case Some(_) => true
          case None => false
        }
      case Failure(_) => false
    }
  }
}
