package persistence

import akka.actor.{ActorLogging, Props}
import models.User
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.BSONDocument

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

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

  var driver: MongoDriver = null
  var connection: MongoConnection = null
  var db: DefaultDB = null
  var users: BSONCollection = null

  def withMongoConnection[T](body: => T): Try[T] = {
    Try{
      if(users == null){
        driver = new MongoDriver(context.system)
        connection = driver.connection(Seq("localhost"))
        db = connection.db(db_name)
        users = db.collection[BSONCollection](collection_name)
        users.indexesManager.ensure(Index(List(("name", Ascending)), unique = true))
      }

      body
    }
  }

  override def createUser(user: User): Option[Int] = {
    withMongoConnection {
      Await.result(
      users.update(
      BSONDocument("username" -> user.username),
      user,
      upsert = true
      ).map {
        lastError =>
        lastError.ok match {
          case true => user.id
          case false => throw new Exception(lastError)
        }
      }, 5.seconds
      )
    } match {
      case Success(id) => //TODO whats new_user
        Some(id)
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

  override def updateUser(user: User): Boolean = ???

  override def readUser(id: Int): Option[User] = {
    withMongoConnection {
      val query = BSONDocument("id" -> id)
      Await.result(users.find(query).one[User], 5.seconds)
    } match {
      case Success(user) => user
      case Failure(_) => None
    }
  }
}
